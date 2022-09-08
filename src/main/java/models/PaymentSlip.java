package models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class PaymentSlip {

    private final String id;
    private final LocalDate dueDate;
    private final BankCustomer payer;
    private final BankCustomer payee;
    private final BigDecimal value;
    private final String barcode;

}
