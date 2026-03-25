package dtu.pay.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;

import dtu.pay.events.PaymentFailedRecorded;
import dtu.pay.events.PaymentInitiated;
import dtu.pay.events.PaymentRecorded;
import lombok.Getter;
import messaging.Event;

@AggregateRoot
@Entity
@Getter
public class Payment {
    private PaymentId paymentId;
    private String token;
    private AccountId merchantId;
    private AccountId customerId;
    private int amount;
    private PaymentStatus status;
    private String receiptId;
    private String failureReason;

    private transient final List<Event> appliedEvents = new ArrayList<>();
    private transient final Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();

    // @author Tobias
    public static Payment initiate(String correlationId, String token, AccountId merchantId, AccountId customerId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount needs to be positive");
        }
        PaymentId paymentId = new PaymentId(java.util.UUID.randomUUID());
        PaymentInitiated event = new PaymentInitiated(correlationId, paymentId, token, merchantId, customerId, amount);
        Payment payment = new Payment();
        payment.apply(event);
        payment.appliedEvents.add(event);
        return payment;
    }

    // @author Christoffer
    public static Payment initiate(String correlationId, String token, String merchantId, String customerId, int amount) {
        return initiate(correlationId, token, AccountId.fromString(merchantId), AccountId.fromString(customerId), amount);
    }

    // @author Peter
    public static Payment createFromEvents(Stream<Event> events) {
        Payment payment = new Payment();
        payment.applyEvents(events);
        return payment;
    }

    // @author Fabian
    public static Payment fromRecordedEvent(PaymentRecorded event) {
        Payment payment = new Payment();
        payment.apply(event);
        return payment;
    }

    public Payment() {
        registerEventHandlers();
    }

    // @author Frederik
    public void markCompleted(String receiptId) {
        if (merchantId == null || customerId == null) {
            throw new IllegalStateException("Payment is missing account details");
        }
        PaymentRecorded event = new PaymentRecorded(paymentId, receiptId, token, merchantId, customerId, amount);
        apply(event);
        appliedEvents.add(event);
    }

    // @author Frederik 
    public void markFailed(String reason) {
        PaymentFailedRecorded event = new PaymentFailedRecorded(paymentId, reason);
        apply(event);
        appliedEvents.add(event);
    }

    // @author Nikolaj
    public List<Event> getAppliedEvents() {
        return appliedEvents;
    }

    // @author Nikolaj
    public void clearAppliedEvents() {
        appliedEvents.clear();
    }

    // @author Nikolaj
    private void registerEventHandlers() {
        handlers.put(PaymentInitiated.class, e -> apply((PaymentInitiated) e));
        handlers.put(dtu.pay.events.PaymentCustomerAssigned.class, e -> apply((dtu.pay.events.PaymentCustomerAssigned) e));
        handlers.put(PaymentRecorded.class, e -> apply((PaymentRecorded) e));
        handlers.put(PaymentFailedRecorded.class, e -> apply((PaymentFailedRecorded) e));
    }

    // @author Nikolaj
    private void applyEvents(Stream<Event> events) {
        events.forEachOrdered(this::applyEvent);
        if (paymentId == null) {
            throw new Error("payment does not exist");
        }
    }

    // @author Tobias
    private void applyEvent(Event e) {
        handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
    }

    // @author Tobias
    private void missingHandler(Event e) {
        throw new Error("handler for event " + e + " missing");
    }

    // @author Tobias
    private void apply(PaymentInitiated event) {
        paymentId = event.getPaymentId();
        token = event.getToken();
        merchantId = event.getMerchantId();
        customerId = event.getCustomerId();
        amount = event.getAmount();
        status = PaymentStatus.PENDING;
    }

    // @author Frederik
    public void assignCustomer(AccountId accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Customer account is required");
        }
        if (customerId != null && !customerId.equals(accountId)) {
            throw new IllegalStateException("Customer account already assigned");
        }
        dtu.pay.events.PaymentCustomerAssigned event = new dtu.pay.events.PaymentCustomerAssigned(paymentId, accountId);
        apply(event);
        appliedEvents.add(event);
    }

    // @author Fabian
    private void apply(PaymentRecorded event) {
        paymentId = event.getPaymentId();
        token = event.getToken();
        merchantId = event.getMerchantId();
        customerId = event.getCustomerId();
        amount = event.getAmount();
        receiptId = event.getReceiptId();
        status = PaymentStatus.COMPLETED;
    }

    // @author Peter
    private void apply(dtu.pay.events.PaymentCustomerAssigned event) {
        paymentId = event.getPaymentId();
        customerId = event.getCustomerId();
    }

    // @author Peter
    private void apply(PaymentFailedRecorded event) {
        failureReason = event.getReason();
        status = PaymentStatus.FAILED;
    }
}
