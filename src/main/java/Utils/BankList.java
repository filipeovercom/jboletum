package Utils;

import lombok.Data;
import models.BankCustomer;
import models.PaymentSlip;

import java.util.ArrayList;
import java.util.List;

@Data
public class BankList {

    List<BankCustomer> customers = new ArrayList<>();
    List<PaymentSlip> paymentSlips = new ArrayList<>();



}
