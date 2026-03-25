package dtu.pay.events;

import dtu.pay.aggregate.PaymentId;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Ksawery
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class PaymentFailedRecorded extends Event {

    private PaymentId paymentId;

    private String reason;


    public PaymentFailedRecorded(PaymentId paymentId, String reason) {
        super("PaymentFailedRecorded", paymentId, reason);
        this.paymentId = paymentId;
        this.reason = reason;
    }
}
