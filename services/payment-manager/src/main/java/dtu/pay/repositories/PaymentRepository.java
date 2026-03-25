package dtu.pay.repositories;

import org.jmolecules.ddd.annotation.Repository;


import dtu.pay.aggregate.Payment;
import java.util.List;

import dtu.pay.aggregate.PaymentId;
import dtu.pay.events.PaymentFailedRecorded;
import dtu.pay.events.PaymentInitiated;
import dtu.pay.events.PaymentCustomerAssigned;
import dtu.pay.events.PaymentRecorded;
import messaging.MessageQueue;
import messaging.Event;

@Repository
public class PaymentRepository {

    private final EventStore eventStore;

    public PaymentRepository(MessageQueue bus) {
        this.eventStore = new EventStore(bus);
    }

    // @author Nikolaj
    public Payment getById(PaymentId paymentId) {
        return Payment.createFromEvents(eventStore.getEventsFor(paymentId));
    }

    // @author Nikolaj
    public void save(Payment payment) {
        List<Event> appliedEvents = payment.getAppliedEvents();
        if (appliedEvents.isEmpty()) {
            return;
        }
        PaymentId paymentId = extractPaymentId(appliedEvents);
        eventStore.addEvents(paymentId, appliedEvents);
        payment.clearAppliedEvents();
    }

    // @author Frederik
    private PaymentId extractPaymentId(List<Event> events) {
        for (Event event : events) {
            if (event instanceof PaymentInitiated initiated) {
                return initiated.getPaymentId();
            }
            if (event instanceof PaymentCustomerAssigned assigned) {
                return assigned.getPaymentId();
            }
            if (event instanceof PaymentRecorded recorded) {
                return recorded.getPaymentId();
            }
            if (event instanceof PaymentFailedRecorded failed) {
                return failed.getPaymentId();
            }
        }
        throw new IllegalStateException("Payment events missing paymentId");
    }
}
