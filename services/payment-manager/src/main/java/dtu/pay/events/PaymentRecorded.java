package dtu.pay.events;

import dtu.pay.aggregate.AccountId;
import dtu.pay.aggregate.PaymentId;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Tobias
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class PaymentRecorded extends Event {
    private static final long serialVersionUID = 1L;

    private PaymentId paymentId;

    private String receiptId;

    private String token;

    private AccountId merchantId;

    private AccountId customerId;

    private int amount;

    public PaymentRecorded(PaymentId paymentId, String receiptId, String token, AccountId merchantId, AccountId customerId, int amount) {
        super("PaymentRecorded", paymentId, receiptId, token, merchantId, customerId, amount);
        this.paymentId = paymentId;
        this.receiptId = receiptId;
        this.token = token;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.amount = amount;
    }
}
