package tarefas;

import lombok.extern.slf4j.Slf4j;
import models.PaymentSlip;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class EmitirBoleto implements Supplier<Map<String, String>> {

    private static Map<String, String> executaEmissaoDeBoletos(Path diretorio) {
        Map<String, String> relatorio = new HashMap<>();

        try (var files = Files.list(diretorio)) {

            AtomicInteger boletosSucesso = new AtomicInteger(0);

            files.toList()
                    .parallelStream()
                    .filter(EmitirBoleto::filtraArquivosRemessa)
                    .forEach(file -> {
                        log.info("Executando for para thread {}", Thread.currentThread().getName());
                        log.info("Arquivo filtrado: {}", file.getFileName().toString());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            log.error("Erro ao adcionar delay!");
                        }
                        boletosSucesso.addAndGet(extrairLinhasDoArquivo(file));
                    });

            relatorio.put(
                    "Boletos processados com sucesso:", String.valueOf(boletosSucesso.get()));
        } catch (IOException e) {
            log.error("Deu erro!!");
        }

        return relatorio;
    }

    private static int extrairLinhasDoArquivo(Path file) {
        AtomicInteger boletosSucesso = new AtomicInteger(0);
        try (var linhas = Files.lines(file)) {
            var codigosDeBarra = linhas.map(EmitirBoleto::transformaLinhaEmBoleto)
                    .map(EmitirBoleto::gerarCodigoDeBarras)
                    .peek(boleto -> boletosSucesso.addAndGet(1))
                    .collect(Collectors.toList());
            gerarArquivoRetorno(file.getFileName().toString(), codigosDeBarra);
        } catch (IOException e) {
            log.error("Deu erro ao ler linhas do arquivo");
        }
        return boletosSucesso.get();
    }

    private static void gerarArquivoRetorno(String nomeArquivoRemessa, List<String> codigosDeBarra) {
    }

    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        return "";
    }

    private static PaymentSlip transformaLinhaEmBoleto(String linha) {
        String[] valores = linha.split(";");

        var paymentSlip = PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(LocalDate.parse(valores[0])) // TODO -> Transformar string em LocalDate
                .value(new BigDecimal(valores[5]))
                .build();

        return paymentSlip;
    }

    private static boolean filtraArquivosRemessa(Path file) {
        return file.getFileName().toString().endsWith(".rem");
    }

    @Override
    public Map<String, String> get() {

        Instant startTime = Instant.now();

        Path diretorio = Paths.get("");

        var relatorio = executaEmissaoDeBoletos(diretorio);

        Instant endTime = Instant.now();

        long duration = Duration.between(startTime, endTime).toMillis();

        relatorio.put("Tempo de Execução:", String.format("%dms", duration));

        return relatorio;
    }
}
