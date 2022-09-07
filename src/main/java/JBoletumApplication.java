import lombok.extern.slf4j.Slf4j;
import models.Menu;
import tarefas.EmitirBoleto;
import tarefas.LiquidarBoleto;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class JBoletumApplication {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        System.out.println("Application is running...");
        System.out.println("INFO: Type exit and press enter to close the application!");

        exibeMenu();

        escolherTarefa(
                JBoletumApplication::capturaEntradaScanner,
                JBoletumApplication::identificaMenu);

//        CompletableFuture<String> futureString = CompletableFuture.supplyAsync(() -> {
//            try {
//                log.info(Thread.currentThread().getName());
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            return "Filipe assíncrono";
//        });
//
//        futureString.thenAccept(log::info);
////        log.info(futureString.get());
//
//        var tarefaEmitirBoletos = new EmitirBoleto();
//        var tarefaLiquidarBoletos = new LiquidarBoleto();
//
//        CompletableFuture<Void> futuroEmissao = CompletableFuture.runAsync(tarefaEmitirBoletos);
//        CompletableFuture<Void> futuroLiquidacao = CompletableFuture.runAsync(tarefaLiquidarBoletos);
//
//        futuroEmissao.thenRun(() -> log.info("O processamento de emissão acabou"));
//        futuroLiquidacao.thenRun(() -> log.info("O processamento de liquidação acabou"));
//
//        Thread.sleep(5000);
    }

    private static Optional<Menu> identificaMenu(String valorCapturado) {
        return Optional.ofNullable(valorCapturado)
                .filter(valor -> !valor.isBlank())
                .map(Integer::parseInt)
                .map(indice -> Menu.values()[indice]);
    }

    private static String capturaEntradaScanner() {
        return new Scanner(System.in).nextLine();
    }

    private static void exibeMenu() {
        Stream.of(Menu.values())
                .map(JBoletumApplication::formataEntradaMenu)
                .forEach(System.out::println);
    }

    private static String formataEntradaMenu(Menu menu) {
        var formato = "[%d] - %s";
        return String.format(formato, menu.ordinal(), menu.getNome());
    }

    private static void escolherTarefa(Supplier<String> funcaoCapturaEntrada,
                                       Function<String, Optional<Menu>> funcaoIdentificaMenu) {
        funcaoIdentificaMenu.apply(funcaoCapturaEntrada.get())
                .map(menu -> {
                    log.info("Executando tarefa [{}]", menu.getNome());
                    return menu.getTarefa();
                })
                .ifPresentOrElse(tarefa -> {
                    CompletableFuture.supplyAsync(tarefa)
                            .thenAccept(relatorio ->
                                relatorio.entrySet().stream()
                                .map(entrada -> String.format("%s %s", entrada.getKey(), entrada.getValue()))
                                .forEach(log::warn));
                    exibeMenu();
                    escolherTarefa(funcaoCapturaEntrada, funcaoIdentificaMenu);
                }, () -> {
                    System.out.println("Menu não encontrado para o valor de entrada!!");
                    escolherTarefa(funcaoCapturaEntrada, funcaoIdentificaMenu);
                });
    }
}
