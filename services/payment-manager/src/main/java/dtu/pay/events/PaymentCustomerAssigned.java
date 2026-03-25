package dtu.pay.events;

import dtu.pay.aggregate.AccountId;
import dtu.pay.aggregate.PaymentId;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Ksawery
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class PaymentCustomerAssigned extends Event {
    private static final long serialVersionUID = 1L;

    private PaymentId paymentId;
    private AccountId customerId;

    public PaymentCustomerAssigned(PaymentId paymentId, AccountId customerId) {
        super("PaymentCustomerAssigned", paymentId, customerId);
        this.paymentId = paymentId;
        this.customerId = customerId;
    }
}
