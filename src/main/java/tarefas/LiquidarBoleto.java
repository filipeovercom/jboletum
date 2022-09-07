package tarefas;


import lombok.extern.slf4j.Slf4j;
import models.BankCustomer;
import models.PaymentSlip;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
public class LiquidarBoleto implements Runnable {

    static List<LocalDate> ok = new ArrayList<>();
    static List<String> erro = new ArrayList<>();
    static List<LocalDate> vencidos = new ArrayList<>();
    static long duration;
    @Override
    public void run() {
        Instant start = Instant.now();
        Path diretorio = Paths.get("");

        executaLiquidacaoDeBoletos(diretorio);


        geraRelatorios(start);
    }

    private void geraRelatorios(Instant instant) {
        long timeElapsed = Instant.now().toEpochMilli() - (instant.toEpochMilli());
        System.out.println("Tempo de execução: " + timeElapsed);
        int lidos = ok.size() + vencidos.size() + erro.size();
        System.out.println("Total de boletos lidos: " + lidos);
        System.out.println("Total de boletos liquidados com sucesso: " + ok.size());
        System.out.println("Total de boletos invalidos: " + erro.size());
        System.out.println("Total de boletos vencidos: " + vencidos.size());
        ok.clear();
        vencidos.clear();
        erro.clear();
    }

    private void executaLiquidacaoDeBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {

            files
                    .filter(LiquidarBoleto::filtraArquivosRetorno)
                    .forEach(LiquidarBoleto::extrairLinhasDoArquivo);

        } catch (IOException e) {
            System.out.println("Erro importação para liquidacao");
        }
    }

    private static void extrairLinhasDoArquivo(Path path) {
        try (var linha = Files.lines(path)) {
            var pagamentos = linha
                   .filter(LiquidarBoleto::filtraArquivosOK)
                  .map(LiquidarBoleto::montaBoleto)
                    .filter(LiquidarBoleto::verificaBoletoVencido)
                    .map(LiquidarBoleto::geraBoletoPago)
                    .collect(Collectors.toList());

            geraArquivodePagamento(path.getFileName().toString(), pagamentos);



        } catch (IOException e) {
            System.out.println("Deu erro ao ler linhas do arquivo");
        }
    }

    private static boolean verificaBoletoVencido(PaymentSlip boleto) {
        if(!boletoVencido(boleto)) {
            ok.add(boleto.getDueDate());
            return true;
        } else {
            vencidos.add(boleto.getDueDate());
            return false;
        }


    }

    private static boolean boletoVencido(PaymentSlip boleto) {
        return boleto.getDueDate().isBefore(LocalDate.now());
    }

    private static void geraArquivodePagamento(String nomeArquivoRetorno, List<String> pagamentos) throws IOException {
        Path novoArquivo = Paths.get(nomeArquivoRetorno.replace(".rem",".ret"));

        Files.write(novoArquivo,pagamentos);
    }


    private static String geraBoletoPago(PaymentSlip boleto) {
        liquidaBoletos(boleto);

        return String.format("%s%s%s%s%s%s%s,Pago",
                boleto.getId(),
                boleto.getDueDate().toString().replace("-",""),
                boleto.getPayer().getAgencyNumber(),
                boleto.getPayer().getAccountNumber(),
                boleto.getPayee().getAgencyNumber(),
                boleto.getPayee().getAccountNumber(),
                boleto.getValue());
    }

    private static void liquidaBoletos(PaymentSlip paymentSlip) {
        paymentSlip.
                toBuilder()
                .payer(paymentSlip.getPayer()
                        .toBuilder()
                        .balance(paymentSlip.getPayer()
                                .getBalance()
                                .subtract(paymentSlip.getValue()))
                        .build())
                .payee(paymentSlip.getPayee().
                        toBuilder()
                        .balance(paymentSlip.getPayee()
                                .getBalance()
                                .add(paymentSlip.getValue()))
                        .build())
                .build();
    }

    private static PaymentSlip montaBoleto(String linha) {
        String[] valores = linha.split(",");

        BankCustomer payee = BankCustomer.builder()
                .id(UUID.randomUUID().toString())
                .agencyNumber(valores[2])
                .accountNumber(valores[3])
                .balance(new BigDecimal(valores[7]))
                .build();
        BankCustomer payer = BankCustomer.builder()
                .id(UUID.randomUUID().toString())
                .agencyNumber(valores[4])
                .accountNumber(valores[5])
                .balance(new BigDecimal(valores[8]))
                .build();
        return PaymentSlip.builder()
                .id(valores[0])
                .dueDate(LocalDate.parse(valores[1], DateTimeFormatter.BASIC_ISO_DATE))
                .value(new BigDecimal(valores[6]))
                .payee(
                        BankCustomer.builder()
                                .id(UUID.randomUUID().toString())
                                .agencyNumber(valores[2])
                                .accountNumber(valores[3])
                                .balance(new BigDecimal(valores[7]))
                                .build())
                .payer(
                        BankCustomer.builder()
                                .id(UUID.randomUUID().toString())
                                .agencyNumber(valores[4])
                                .accountNumber(valores[5])
                                .balance(new BigDecimal(valores[8]))
                                .build())
                .build();
    }

    private static boolean filtraArquivosOK(String s) {
        if (!s.endsWith("OK")) {
            erro.add("erro");
        }
        return s.endsWith("OK");
    }



    private static boolean filtraArquivosRetorno(Path path) {
        return path.getFileName().toString().endsWith("liquidacao.rem");
    }
}
