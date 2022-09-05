package models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class BankCustomer {

    private final String id;
    private final String name;
    private final String agencyNumber;
    private final String accountNumber;
    private final BigDecimal balance;

}
