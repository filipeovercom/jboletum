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

public class JBoletumApplication {

    public static void main(String[] args) {

        System.out.println("Iniciando...");

        exibeMenu();

        escolherTarefa(
                JBoletumApplication::capturaEntradaScanner,
                JBoletumApplication::identificaMenu);
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
                .map(Menu::getTarefa)
                .ifPresentOrElse(Runnable::run, () -> {
                    System.out.println("Menu não encontrado para o valor de entrada!!");
                    escolherTarefa(funcaoCapturaEntrada, funcaoIdentificaMenu);
                });
    }
}