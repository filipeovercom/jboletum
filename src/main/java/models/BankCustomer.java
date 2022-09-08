package models;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class BankCustomer {

    private final String id;
    private final String name;
    private final String agencyNumber;
    private final String accountNumber;
    private BigDecimal balance;

}