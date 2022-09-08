package tarefas;

import models.BankCustomer;
import models.PaymentSlip;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LiquidarBoletos implements Runnable {

    @Override
    public void run() {
        var tempoInicial = Instant.now();

        var diretorio = Paths.get("");

        executaLiquidaBoletos(diretorio);

        var tempoFinal = Instant.now();

        var tempoDecorrido = Duration.between(tempoInicial,tempoFinal);

        System.out.println("Tempo total de execução :" + tempoDecorrido.toMillis());

    }

    private static void executaLiquidaBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {
            files
                    .filter(LiquidarBoletos::filtraArquivoRetorno)
                    .peek(file -> System.out.println(file.getFileName()))
                    .forEach(file -> {
                        try (var linhas = Files.lines(file)) {
                            var codigosDeBarra = linhas.map(LiquidarBoletos::transformarLinhaEmBoleto)
                                    .map(LiquidarBoletos::transferirValores)
                                    .map(LiquidarBoletos::verificaVencimento)
                                    .map(LiquidarBoletos::gerarCodigoDeBarras)
                                    .collect(Collectors.toList());
                            gerarArquivoRetorno(file.getFileName().toString(), codigosDeBarra);
                        } catch (IOException e) {
                            System.out.println("Deu erro ao ler linhas do arquivo");
                        }
                    });

        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
    }

    private static void gerarArquivoRetorno(String nomeArquivoRemessa, List<String> codigosDeBarra) {
        var novoNomeArquivo = nomeArquivoRemessa.replace("rem","ret");
        var arquivo = Paths.get(novoNomeArquivo);
        try {
            Files.write(arquivo,codigosDeBarra);       // TODO -> Gerar arquivo com códigos de barras ([mesmo nome do arquivo remessa].ret
        }catch (Exception e){
            System.out.println("Erro ao gravar no " + nomeArquivoRemessa);
        }
    }
    private static boolean filtraArquivoRetorno(Path file) {
        return file.getFileName().toString().endsWith("Liquidação.rem");
    }



    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        var s = paymentSlip.getId();
        s += paymentSlip.getDueDate().toString();
        s += paymentSlip.getValue();
        s += paymentSlip.getPayee().getBalance();
        s += paymentSlip.getPayer().getBalance();
        s += paymentSlip.getBarcode();
        return s; // TODO -> criar codigo a partir das informações do boleto
    }

    private static PaymentSlip transformarLinhaEmBoleto(String linha) {
        String[] valores = linha.split(";");
        return PaymentSlip.builder()
                .id(valores[0])
                .dueDate(LocalDate.parse(valores[1]))
                .value(new BigDecimal(valores[2]))
                .payee(BankCustomer.builder()
                        .balance(new BigDecimal(valores[5])).build())
                .payer(BankCustomer.builder()
                        .balance(new BigDecimal(valores[6])).build())
                .build();
    }


    private static PaymentSlip transferirValores(PaymentSlip boleto){
        BankCustomer payer = boleto.getPayer();

        BankCustomer payee = boleto.getPayee();

        if(boleto.getValue().compareTo(boleto.getPayee().getBalance())>0){
            return boleto.toBuilder()
                    .barcode("Saldo insulfiente :")
                    .build();
        }

        return boleto.toBuilder()
                .payer(payer.toBuilder()
                        .balance(payer.getBalance().subtract(boleto.getValue())).build())
                .payee(payee.toBuilder()
                        .balance(payee.getBalance().add(boleto.getValue())).build())
                .build();
    }

    private static PaymentSlip verificaVencimento(PaymentSlip boleto){
        LocalDate hoje = LocalDate.now();

        final TemporalQuery<Boolean> verificaVencimento = temporal -> hoje.compareTo(boleto.getDueDate()) < 0;

        if (hoje.query(verificaVencimento)){
            return boleto.toBuilder()
                    .barcode("Data valida")
                    .build();
        }
        return boleto.toBuilder()
                .barcode("Boleto vencido")
                .build();
    }
}
