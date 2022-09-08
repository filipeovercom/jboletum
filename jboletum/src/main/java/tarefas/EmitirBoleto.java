package tarefas;

import models.PaymentSlip;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class EmitirBoleto implements Runnable {

    @Override
    public void run() {
        var tempoInicial = Instant.now();

        var diretorio = Paths.get("");

        var numeroBoletos = executaEmissaoBoleto(diretorio);

        var tempoFinal = Instant.now();

        var tempoDecorrido = Duration.between(tempoInicial,tempoFinal);

        System.out.println("Numeros de boletos processados :" + numeroBoletos + "Tempo total de execução :" + tempoDecorrido.toMillis());
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
                            System.out.println("Deu erro ao ler linhas do arquivo");
                        }
                    });
        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
        return boletosSucesso.get();
    }


    private static void gerarArquivoRetorno(String nomeArquivoRemessa, List<String> codigosDeBarra) {
       var arquivo = Paths.get("Arquivo-Retorno.ret");
       try {
           Files.write(arquivo,codigosDeBarra);       // TODO -> Gerar arquivo com códigos de barras ([mesmo nome do arquivo remessa].ret
       }catch (Exception e){
           System.out.println("Erro ao gravar no " + nomeArquivoRemessa);
       }
    }

    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        var s = paymentSlip.getId();
        s += paymentSlip.getDueDate().toString();
        s += paymentSlip.getPayee().getName();
        s += paymentSlip.getValue();
        s += paymentSlip.getBarcode();
        return s; // TODO -> criar codigo a partir das informações do boleto
    }

    private static PaymentSlip transformaLinhaEmBoleto(String linha) {
        String[] valores = linha.split(";");
        return PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(ajustarDataVencimentoFinaisDeSemana(LocalDate.parse(valores[0]))) // TODO -> Transformar string em LocalDate
                .value(new BigDecimal(valores[1]))
                .build();
    }

    private static boolean filtraArquivosRemessa(Path file) {
        return file.getFileName().toString().equalsIgnoreCase("Arquivo-Remessa.rem");
    }

    private static LocalDate ajustarDataVencimentoFinaisDeSemana(LocalDate dataVencimento){
        var ajuste = TemporalAdjusters.next(DayOfWeek.MONDAY);
        if (dataVencimento.getDayOfWeek() == DayOfWeek.SATURDAY) {
          return dataVencimento.with(ajuste);
        } else if (dataVencimento.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return dataVencimento.with(ajuste);
        } else
            return dataVencimento;
    }

    private static PaymentSlip checaDataValida(PaymentSlip boleto){
        if (boleto.getDueDate().isBefore(LocalDate.now())){
            return boleto.toBuilder()
                    .barcode("Data invalida para emissão")
                    .build();
        }
        return boleto.toBuilder()
                .barcode("OK")
                .build();
    }


}