package tarefas;

import lombok.extern.slf4j.Slf4j;
import models.PaymentSlip;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EmitirBoleto implements Runnable {

    @Override
    public void run() {
        var inicio = Instant.now();
        var diretorio = Paths.get("");
        var qtdBoletos = executaEmissaoBoleto(diretorio);
        var fim = Instant.now();
        var duracao = Duration.between(inicio,fim);

        System.out.print("Tempo de execução: " + duracao.toMillis());
    }

    private static int executaEmissaoBoleto(Path diretorio) {
        var boletosSucesso = new AtomicInteger(0);
        try (var files = Files.list(diretorio)) {
            files
                    .filter(EmitirBoleto::filtraArquivosRemessa)
                    .peek(file -> System.out.println(file.getFileName()))
                    .forEach(file -> {
                        try (var linhas = Files.lines(file)) {
                            var codigosDeBarra = linhas.map(EmitirBoleto::transformaLinhaEmBoleto)
                                    .map(EmitirBoleto::checaDataValida)
                                    .map(EmitirBoleto::gerarCodigoDeBarras)
                                    .collect(Collectors.toList());
                            boletosSucesso.addAndGet(1);
                            gerarArquivoRetorno(file.getFileName().toString(), codigosDeBarra);
                        } catch (IOException e) {
                            System.out.println("Erro ao processar linhas do arquivo");
                        }
                    });
        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
        return boletosSucesso.get();
    }


    private static void gerarArquivoRetorno(String nomeArquivoRemessa, List<String> codigosDeBarra) {
        var arquivo = Paths.get("retorno.ret");
        try {
            Files.write(arquivo,codigosDeBarra);
        }catch (Exception e){
            System.out.println("Erro ao gravar no " + nomeArquivoRemessa);
        }
    }

    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        var slip = paymentSlip.getId();
        slip += paymentSlip.getDueDate().toString();
        slip += paymentSlip.getValue();
        slip += paymentSlip.getBarcode();
        return slip;
    }

    private static PaymentSlip transformaLinhaEmBoleto(String linha) {
        String[] valores = linha.split(";");
        return PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(ajustarData(LocalDate.parse(valores[0])))
                .value(new BigDecimal(valores[1]))
                .build();
    }

    private static boolean filtraArquivosRemessa(Path file) {
        return file.getFileName().toString().equalsIgnoreCase("remessa.rem");
    }

    private static LocalDate ajustarData(LocalDate vencimento){
        var nova_data = TemporalAdjusters.next(DayOfWeek.MONDAY);
        if (vencimento.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return vencimento.with(nova_data);
        } else if (vencimento.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return vencimento.with(nova_data);
        } else
            return vencimento;
    }

    private static PaymentSlip checaDataValida(PaymentSlip boleto){
        if (boleto.getDueDate().isBefore(LocalDate.now())){
            return boleto.toBuilder().barcode(" - Data inválida!").build();
        }
        return boleto.toBuilder().barcode(" - Ok").build();
    }


}