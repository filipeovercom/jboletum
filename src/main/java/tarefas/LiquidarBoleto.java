package tarefas;

import Utils.BankList;
import com.sun.source.tree.TryTree;
import lombok.extern.slf4j.Slf4j;
import models.BankCustomer;
import models.PaymentSlip;

import javax.sound.midi.Soundbank;
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
public class LiquidarBoleto implements Runnable {

    @Override
    public void run() {
        Path diretorio = Paths.get("");

        executaLiquidacaoDeBoletos(diretorio);
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
                  .map(LiquidarBoleto::verificaBoleto)
                    .map(LiquidarBoleto::geraBoletoPago).toList();

            geraArquivodePagamento(path.getFileName().toString(), pagamentos);



        } catch (IOException e) {
            System.out.println("Deu erro ao ler linhas do arquivo");
        }
    }

    private static void geraArquivodePagamento(String nomeArquivoRetorno, List<String> pagamentos) throws IOException {
        Path novoArquivo = Paths.get(nomeArquivoRetorno.replace("emissao.ret","")+"pagamento.ret");

        Files.write(novoArquivo,pagamentos);
    }


    private static String geraBoletoPago(PaymentSlip boleto) {
        liquidaBoletos(boleto);

        return String.format("%s%s%s%s%s%s%s%s",
                boleto.getId(),
                boleto.getDueDate().toString().replace("-",""),
                boleto.getPayer().getAgencyNumber(),
                boleto.getPayer().getAccountNumber(),
                boleto.getPayee().getAgencyNumber(),
                boleto.getPayee().getAccountNumber(),
                boleto.getValue(),
                ";Pago");
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

    private static PaymentSlip verificaBoleto(String s) {
   //   System.out.println(s.substring(0,36));
   //   System.out.println(s.substring(36,44));
   //   System.out.println(s.substring(44,48));
   //   System.out.println(s.substring(48,53));
   //   System.out.println(s.substring(53,57));
   //   System.out.println(s.substring(57,62));
   //   System.out.println(s.substring(62,65));
   //   System.out.println(s.substring(65,67));

    //   boleto.getId(),
    //           boleto.getDueDate().toString().replace("-",""),
    //                   boleto.getPayer().getAgencyNumber(),
    //                   boleto.getPayer().getAccountNumber(),
    //                   boleto.getPayee().getAgencyNumber(),
    //                   boleto.getPayee().getAccountNumber(),
    //                   boleto.getValue(),   s.substring(40,42)  s.substring(42,44)
        //LocalDate.of(Integer.parseInt(s.substring(36,40)),Integer.parseInt(s.substring(40,42)),Integer.parseInt(s.substring(42,44)) )
        PaymentSlip boleto = PaymentSlip.builder()
                .id(s.substring(0,36))
                .dueDate(LocalDate.of(Integer.parseInt(s.substring(36,40)),Integer.parseInt(s.substring(40,42)),Integer.parseInt(s.substring(42,44)) ))
                .payer(BankCustomer.builder()
                        .agencyNumber(s.substring(44,48))
                        .accountNumber(s.substring(48,53))
                        .balance(BigDecimal.valueOf(300))
                        .build())
                .payee(BankCustomer.builder()
                        .agencyNumber(s.substring(53,57))
                        .accountNumber(s.substring(57,62))
                        .balance(BigDecimal.valueOf(0))
                        .build())
                .value(BigDecimal.valueOf(Long.parseLong((s.substring(62,65)))))
                .build();
        return boleto;
    }

    private static boolean filtraArquivosOK(String s) {
        return s.endsWith("OK");
    }



    private static boolean filtraArquivosRetorno(Path path) {
        return path.getFileName().toString().endsWith("ret");
    }
}
