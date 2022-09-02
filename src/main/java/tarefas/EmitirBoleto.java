package tarefas;

import lombok.extern.slf4j.Slf4j;
import models.PaymentSlip;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class EmitirBoleto implements Runnable {

    @Override
    public void run() {

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(4));

        Path diretorio = Paths.get("");

        executaEmissaoDeBoletos(diretorio);
    }

    private static void executaEmissaoDeBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {

            files.toList()
                    .parallelStream()
                    .filter(EmitirBoleto::filtraArquivosRemessa)
                    .forEach(file -> {
                        log.info("Executando for para thread {}", Thread.currentThread().getName());
                        log.info("Arquivo filtrado: {}", file.getFileName().toString());
                        extrairLinhasDoArquivo(file);
                    });

        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
    }

    private static void extrairLinhasDoArquivo(Path file) {
        try (var linhas = Files.lines(file)) {
            var codigosDeBarra = linhas.map(EmitirBoleto::transformaLinhaEmBoleto)
                    .map(EmitirBoleto::gerarCodigoDeBarras)
                    .collect(Collectors.toList());
            gerarArquivoRetorno(file.getFileName().toString(), codigosDeBarra);
        } catch (IOException e) {
            System.out.println("Deu erro ao ler linhas do arquivo");
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
