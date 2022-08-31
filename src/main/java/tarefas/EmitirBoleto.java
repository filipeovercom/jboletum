package tarefas;

import models.PaymentSlip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmitirBoleto implements Runnable {

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        Path diretorio = Paths.get("");

        try (var files = Files.list(diretorio)) {
            files
                    .filter(EmitirBoleto::filtraArquivosRemessa)
                    .peek(file -> System.out.println(file.getFileName()))
                    .forEach(file -> {
                        try (var linhas = Files.lines(file)) {
                            var codigosDeBarra = linhas.map(EmitirBoleto::transformaLinhaEmBoleto)
                                    .map(EmitirBoleto::gerarCodigoDeBarras)
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
        // TODO -> Gerar arquivo com códigos de barras ([mesmo nome do arquivo remessa].ret
    }

    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        return ""; // TODO -> criar codigo a partir das informações do boleto
    }

    private static PaymentSlip transformaLinhaEmBoleto(String linha) {
        String[] valores = linha.split(";");

        return PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(LocalDate.now()) // TODO -> Transformar string em LocalDate
                .value(new BigDecimal(valores[1]))
                .build();
    }

    private static boolean filtraArquivosRemessa(Path file) {
        return file.getFileName().toString().endsWith(".rem");
    }
}
