import models.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class JBoletumFuncApplication {

    public static void main(String[] args){

        System.out.println("Application is running...");
        System.out.println("INFO: Type exit and press enter to close the application!");

        BankCustomer payer = BankCustomer.builder()
                .id(UUID.randomUUID().toString())
                .build();

        BankCustomer payee = BankCustomer.builder()
                .id(UUID.randomUUID().toString())
                .build();

        PaymentSlip boletoCasasBahia = PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(LocalDate.now().plusMonths(1))
                .value(new BigDecimal("299.99"))
                .payer(null)
                .payee(payee)
                .build();

        String accountType = "CONTA_CORRENTE";

        NossaRunnable run = () -> System.out.println("Nome errado!");

        var taxaDeJuros = Optional.ofNullable(accountType)
                .map(String::toUpperCase)
                .flatMap(s -> Optional.of(AccountType.valueOf(s)))
                .flatMap(acc -> Optional.of(acc.getTaxaDeJuros()))
                .orElse(10);

//        executeAte(
//                JBoletumFuncApplication::capturaValorEntrada,
//                JBoletumFuncApplication::verificarValorDeEntrada);
    }

    private static String capturaValorEntrada(Scanner scanner) {
        return scanner.nextLine();
    }

    private static boolean verificarValorDeEntrada(String valorDeEntrada) {
        return Objects.nonNull(valorDeEntrada)
                && !valorDeEntrada.isBlank()
                && valorDeEntrada.equalsIgnoreCase("exit");
    }

    private static void executeAte(Function<Scanner, String> funcaoCapturaValorEntrada,
                                   Function<String, Boolean> funcaoAvaliarCondicaoParada) {
        var escaneadorDoConsole = new Scanner(System.in);
        var valorCapturado = funcaoCapturaValorEntrada.apply(escaneadorDoConsole);

        if(!funcaoAvaliarCondicaoParada.apply(valorCapturado)) {
            System.out.println("WARN: incorrect input value!");
            System.out.println("INFO: Type 'exit' and press enter to close the application!");
            executeAte(funcaoCapturaValorEntrada, funcaoAvaliarCondicaoParada);
        } else {
            System.out.println("INFO: Exiting the application!");
            System.out.println("INFO: Gooooooodbye!");
        }
    }
}
