package tarefas;

import Utils.BankList;
import com.sun.source.tree.TryTree;
import lombok.extern.slf4j.Slf4j;
import models.BankCustomer;
import models.PaymentSlip;

import javax.sound.midi.Soundbank;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EmitirBoleto implements Runnable {

    static List<String> ok = new ArrayList<>();
    static List<String> erro = new ArrayList<>();
    static long duration;
    @Override
    public void run() {
        long start = new Date().getTime();
        Path diretorio = Paths.get("");

        executaEmissaoDeBoletos(diretorio);

       duration = new Date().getTime() - start;

       geraRelatorios();

    }

    private void geraRelatorios() {
        System.out.println("Tempo de execução: " + duration);
        System.out.println("Total de boletos gerados: " + ok.size() + erro.size());
        System.out.println("Total de boletos gerados com sucesso: " + ok.size());
        System.out.println("Total de boletos gerados sem sucesso: " + erro.size());
    }

    /*
        private static void gerarRelatorioBoletos(Path arquivo) throws IOException {
            var linhas = Files.lines(arquivo);
            System.out.println("Total boletos processados " + linhas.count());
            var linha2 = Files.lines(arquivo);
            System.out.println("Total boletos processados com sucesso " + linha2.filter(EmitirBoleto::filtraBoletosOK).count());
            var linha3 = Files.lines(arquivo);
            System.out.println("Total boletos processados sem sucesso " + linha3.filter(EmitirBoleto::filtraBoletosNOK).count());
        }



        private static boolean filtraBoletosOK(String string) {
            return string.equals("OK");
        }
        private static boolean filtraBoletosNOK(String string) {
            return !string.equals("OK");
        }
    */
    private static void executaEmissaoDeBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {

            files
                    .filter(EmitirBoleto::filtraArquivosRemessa)
                    .forEach(EmitirBoleto::extrairLinhasDoArquivo);

        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
    }

    private static void extrairLinhasDoArquivo(Path file) {
        try (var linhas = Files.lines(file)) {
            var codigosDeBarra = linhas.map(EmitirBoleto::transformaLinhaEmBoleto)
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

        Path novoArquivo = Paths.get(nomeArquivoRemessa.replace(".rem","")+".ret");

        Files.write(novoArquivo,codigosDeBarra);

         }
    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        return formataCodigoDeBarras(paymentSlip);

    }
    private static String formataCodigoDeBarras(PaymentSlip boleto){
        var verificacao = Stream.of(boleto).map(EmitirBoleto::VerificaBoleto).toList();

        return String.format("%s%s%s%s%s%s%s%s%s",
                boleto.getId(),
                boleto.getDueDate().toString().replace("-",""),
                boleto.getPayer().getAgencyNumber(),
                boleto.getPayer().getAccountNumber(),
                boleto.getPayee().getAgencyNumber(),
                boleto.getPayee().getAccountNumber(),
                boleto.getValue(),
                ";",
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

        //A FAZER colocar o adjuster para quando a data for posterior e em fim de semana para jogar para o proximo dia util

            if ( LocalDate.from(data).isBefore(LocalDate.now()))
                return false;
            else
        return true ;
    }

    private static boolean filtraArquivosRemessa(Path file) {
        return file.getFileName().toString().endsWith(".rem");
    }


    private static PaymentSlip transformaLinhaEmBoleto(String linha) {
        String[] valores = linha.split(";");

        BankCustomer payee = BankCustomer.builder()
                .id((UUID.randomUUID().toString()))
                .agencyNumber(valores[1])
                .accountNumber(valores[2])
                .balance(new BigDecimal(0))
                .build();
        BankCustomer payer = BankCustomer.builder()
                .id((UUID.randomUUID().toString()))
                .agencyNumber(valores[3])
                .accountNumber(valores[4])
                .balance(new BigDecimal(400))
                .build();
        BankList bankList = new BankList();
        bankList.getCustomers().add(payee);
        bankList.getCustomers().add(payer);
        PaymentSlip boleto = PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(ajustaFDS(convertedata(valores[0])))
                .value(new BigDecimal(valores[5]))
                .payee(payee)
                .payer(payer)
                .build();
        bankList.getPaymentSlips().add(boleto);
        return boleto;
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


