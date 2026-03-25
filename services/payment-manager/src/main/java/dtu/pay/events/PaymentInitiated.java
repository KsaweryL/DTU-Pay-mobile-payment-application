package dtu.pay.events;

import dtu.pay.aggregate.AccountId;
import dtu.pay.aggregate.PaymentId;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Christoffer
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class PaymentInitiated extends Event {

    private String correlationId;

    private PaymentId paymentId;

    private String token;

    private AccountId merchantId;

    private AccountId customerId;

    private int amount;


    public PaymentInitiated(String correlationId, PaymentId paymentId, String token, AccountId merchantId, AccountId customerId, int amount) {
        super("PaymentInitiated", correlationId, paymentId, token, merchantId, customerId, amount);
        this.correlationId = correlationId;
        this.paymentId = paymentId;
        this.token = token;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.amount = amount;
    }
}
