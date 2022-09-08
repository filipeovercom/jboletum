package tarefas;

import lombok.extern.slf4j.Slf4j;
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
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LiquidarBoleto implements Runnable {

    @Override
    public void run() {
        var inicio = Instant.now();
        var diretorio = Paths.get("");
        executaLiquidaBoletos(diretorio);
        var fim = Instant.now();
        var duracao = Duration.between(inicio,fim);

        System.out.println("Tempo de execução :" + duracao.toMillis());
    }

    private static void executaLiquidaBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {
            files
                    .filter(LiquidarBoleto::filtraArquivoRetorno)
                    .peek(file -> System.out.println(file.getFileName()))
                    .forEach(file -> {
                        try (var linhas = Files.lines(file)) {
                            var codigosDeBarra = linhas.map(LiquidarBoleto::transformarLinhaEmBoleto)
                                    .map(LiquidarBoleto::transferirValores)
                                    .map(LiquidarBoleto::verificaVencimento)
                                    .map(LiquidarBoleto::gerarCodigoDeBarras)
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
        return file.getFileName().toString().endsWith("liquidar.rem");
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
            return boleto.toBuilder().barcode("Saldo insufiente").build();
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
            return boleto.toBuilder().barcode("Data é válida").build();
        }
        return boleto.toBuilder().barcode("Boleto está vencido").build();
    }
}