package tarefas;

import Utils.BankList;
import lombok.extern.slf4j.Slf4j;
import models.BankCustomer;
import models.PaymentSlip;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EmitirBoleto implements Runnable {

    static List<String> ok = new ArrayList<>();
    static List<String> erro = new ArrayList<>();

    @Override
    public void run() {

        Instant start = Instant.now();
        Path diretorio = Paths.get("");

        executaEmissaoDeBoletos(diretorio);


        geraRelatorios(start);


    }

    private void geraRelatorios(Instant instant) {
        long timeElapsed = Instant.now().toEpochMilli() - (instant.toEpochMilli());
        int gerados = ok.size() + erro.size();
        System.out.println("Tempo de execução: " + timeElapsed);
        System.out.println("Total de boletos gerados: " + gerados);
        System.out.println("Total de boletos gerados com sucesso: " + ok.size());
        System.out.println("Total de boletos gerados sem sucesso: " + erro.size());
        ok.clear();
        erro.clear();
    }


    private static void executaEmissaoDeBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {

            files
                    .parallel()
                    .filter(EmitirBoleto::filtraArquivosRemessa)
                    .forEach(EmitirBoleto::extrairLinhasDoArquivo);

        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
    }

    private static void extrairLinhasDoArquivo(Path file) {
        try (var linhas = Files.lines(file)) {
            var codigosDeBarra = linhas
                    .map(EmitirBoleto::transformaLinhaEmBoleto)
                    .map(EmitirBoleto::gerarCodigoDeBarras)
                    .map(EmitirBoleto::ajustaTXT)
                    .collect(Collectors.toList());
            gerarArquivoRetorno(file.getFileName().toString(), codigosDeBarra);



        } catch (IOException e) {
            System.out.println("Deu erro ao ler linhas do arquivo");
        }
    }

    private static String ajustaTXT(String s) {
        return s.replace("[", "").replace("]","");
    }

    private static void gerarArquivoRetorno(String nomeArquivoRemessa, List<String> codigosDeBarra) throws IOException {

        Path novoArquivo = Paths.get(nomeArquivoRemessa.replace(".rem",".ret"));

        Files.write(novoArquivo,codigosDeBarra);

        Path novoArquivo2 = Paths.get(nomeArquivoRemessa.replace("emissao","liquidacao"));

        Files.write(novoArquivo2,codigosDeBarra);

         }
    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        return formataCodigoDeBarras(paymentSlip);

    }
    private static String formataCodigoDeBarras(PaymentSlip boleto){
        var verificacao = Stream.of(boleto).map(EmitirBoleto::VerificaBoleto).toList();

        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                boleto.getId(),
                boleto.getDueDate().toString().replace("-",""),
                boleto.getPayee().getAgencyNumber(),
                boleto.getPayee().getAccountNumber(),
                boleto.getPayer().getAgencyNumber(),
                boleto.getPayer().getAccountNumber(),
                boleto.getValue(),
                boleto.getPayee().getBalance(),
                boleto.getPayer().getBalance(),
                verificacao);
    }
    private static String VerificaBoleto(PaymentSlip paymentSlip) {
        if (verificaData(paymentSlip.getDueDate())){
            ok.add("OK");
            return "OK";
        } else {
            erro.add("OK");
            return "Data de vencimento inválida";
        }
    }

    public static Boolean verificaData(LocalDate data) {

        return !LocalDate.from(data).isBefore(LocalDate.now());
    }

    private static boolean filtraArquivosRemessa(Path file) {
        return file.getFileName().toString().endsWith("emissao.rem");
    }


    private static PaymentSlip transformaLinhaEmBoleto(String linha) {
        String[] valores = linha.split(",");

        return PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(ajustaFDS(convertedata(valores[0].replace("-",""))))
                .value(new BigDecimal(valores[5]))
                .payee(
                        BankCustomer.builder()
                                .id((UUID.randomUUID().toString()))
                                .agencyNumber(valores[1])
                                .accountNumber(valores[2])
                                .balance(new BigDecimal(valores[6]))
                                .build())
                .payer(
                        BankCustomer.builder()
                                .id((UUID.randomUUID().toString()))
                                .agencyNumber(valores[3])
                                .accountNumber(valores[4])
                                .balance(new BigDecimal(valores[7]))
                                .build())
                .build();
    }

    private static LocalDate convertedata (String s) {
    return  LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE) ;
}
    private static LocalDate ajustaFDS (LocalDate data) {
        if (verificaData(data) || data.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
        return data.plus(2, ChronoUnit.DAYS) ;
    } else  if (verificaData(data) || data.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            return data.plus(1, ChronoUnit.DAYS) ;
        } else
            {
            return data;
        }
}


}


