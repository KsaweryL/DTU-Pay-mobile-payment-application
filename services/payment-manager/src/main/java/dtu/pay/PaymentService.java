package dtu.pay;

import java.util.concurrent.ConcurrentHashMap;

import dtu.pay.aggregate.AccountId;
import dtu.pay.aggregate.ConcurrentIdState;
import dtu.pay.aggregate.Payment;
import dtu.pay.aggregate.PaymentId;
import dtu.pay.repositories.PaymentReadModelRepository;
import dtu.pay.repositories.PaymentRepository;
import messaging.Event;
import messaging.MessageQueue;

public class PaymentService {

    private final MessageQueue queue;
    private final PaymentRepository paymentRepository;
    private final PaymentReadModelRepository readModelRepository;
    private final ConcurrentHashMap<String, ConcurrentIdState> pendingIdValidations = new ConcurrentHashMap<>();

    public PaymentService(MessageQueue queue) {
        this.queue = queue;
        this.paymentRepository = new PaymentRepository(queue);
        this.readModelRepository = new PaymentReadModelRepository(queue);

        queue.addHandler("TokenInvalid", this::commandTokenInvalid);
        queue.addHandler("TokenValidated", this::commandTokenValidated);
        queue.addHandler("PaymentCompleted", this::commandRecordPaymentCompleted);
        queue.addHandler("TransferFailed", this::commandHandleFailure);

        queue.addHandler("PaymentRequest", this::policyPaymentRequested);
        queue.addHandler("BankAccountDetailsProvided", this::commandBankAccountDetailsProvided);
        queue.addHandler("BankAccountDetailsFailed", this::commandHandleFailure);

        queue.addHandler("GetAllReportRequested", this::commandGetAllReport);
        queue.addHandler("GetCustomerReportRequested", this::commandGetCustomerReport);
        queue.addHandler("GetMerchantReportRequested", this::commandGetMerchantReport);
    }

    // @author Fabian 
    private void commandHandleFailure(Event e) {
        String correlationID = e.getArgument(0, String.class);
        String reason = e.getArgument(1, String.class);
        PaymentId paymentId = readModelRepository.getPendingPaymentId(correlationID);
        if (paymentId != null) {
            Payment payment = paymentRepository.getById(paymentId);
            payment.markFailed(reason);
            paymentRepository.save(payment);
        }
        pendingIdValidations.remove(correlationID);
    }

    // @author Nikolaj
    private void commandTokenInvalid(Event e) {
        String correlationId;
        String reason;
        correlationId = e.getArgument(0, String.class);
        reason = e.getArgument(1, String.class);
        PaymentId paymentId = readModelRepository.getPendingPaymentId(correlationId);
        if (paymentId != null) {
            Payment payment = paymentRepository.getById(paymentId);
            payment.markFailed(reason);
            paymentRepository.save(payment);
        }
        pendingIdValidations.remove(correlationId);

    }

    // @author Tobias
    private void commandGetAllReport(Event event) {
        String correlationId = event.getArgument(0, String.class);
        try {
            queue.publish(new Event("FullReportRequested", correlationId, readModelRepository.getSerializedPayments()));
        } catch (RuntimeException ex) {
            queue.publish(new Event("ReportGenerationFailed", correlationId, ex.getMessage()));
        }
    }

    // @author Tobias
    private void commandGetCustomerReport(Event event) {
        String correlationId = event.getArgument(0, String.class);
        String customerId = event.getArgument(1, String.class);
        try {
            queue.publish(
                new Event(
                    "CustomerFullReportReceived",
                    correlationId,
                    readModelRepository.getSerializedPaymentsByCustomer(customerId)));
        } catch (RuntimeException ex) {
            queue.publish(new Event("ReportGenerationFailed", correlationId, ex.getMessage()));
        }
    }

    // @author Tobias 
    private void commandGetMerchantReport(Event event) {
        String correlationId = event.getArgument(0, String.class);
        String merchantId = event.getArgument(1, String.class);
        try {
            queue.publish(
                new Event(
                    "MerchantFullReportReceived",
                    correlationId,
                    readModelRepository.getSerializedPaymentsByMerchant(merchantId)));
        } catch (RuntimeException ex) {
            queue.publish(new Event("ReportGenerationFailed", correlationId, ex.getMessage()));
        }
    }

    // @author Frederik
    private void commandBankAccountDetailsProvided(Event e) {
        String correlationID = e.getArgument(0, String.class);
        String merchantBankAccount = e.getArgument(1, String.class);
        String customerBankAccount = e.getArgument(2, String.class);
        updatePaymentState(correlationID, state -> {
            state.BankCustomerId = customerBankAccount;
            state.BankMerchantId = merchantBankAccount;
        });
    }

    // @author Ksawery
    private void policyPaymentRequested(Event e) {
        String correlationID = e.getArgument(0, String.class);
        Number amountNumber = e.getArgument(1, Number.class);
        int amount = amountNumber.intValue();
        String merchantId = e.getArgument(2, String.class);
        String token = e.getArgument(3, String.class);
        if (amount <= 0) {
            queue.publish(new Event("PaymentFailed", correlationID, "Amount needs to be positive"));
            queue.publish(new Event("ReleaseTokenRequested", correlationID));
            return;
        }

        commandCreatePayment(correlationID, amount, merchantId, token);
    }

    // @author Ksawery
    private void updatePaymentState(String correlationId, java.util.function.Consumer<ConcurrentIdState> updater) {
        ConcurrentIdState state = pendingIdValidations.computeIfAbsent(correlationId, id -> new ConcurrentIdState());
        synchronized (state) {
            updater.accept(state);
            if (state.isReady() && !state.transferRequested) {
                state.transferRequested = true;
                queue.publish(new Event("TransferRequested", correlationId,
                        state.BankMerchantId, state.BankCustomerId,
                        state.amount));
            }
        }
    }

    // @author Tobias
    private void commandTokenValidated(Event e) {
        String correlationId = e.getArgument(0, String.class);
        String customerId = e.getArgument(1, String.class);
        PaymentId paymentId = readModelRepository.getPendingPaymentId(correlationId);
        if (paymentId == null) {
            ConcurrentIdState state = pendingIdValidations.get(correlationId);
            if (state != null) {
                paymentId = state.paymentId;
            }
        }
        if (paymentId != null) {
            Payment payment = paymentRepository.getById(paymentId);
            if (payment.getCustomerId() == null) {
                payment.assignCustomer(AccountId.fromString(customerId));
                paymentRepository.save(payment);
            }
        }
        updatePaymentState(correlationId, state -> state.customerId = customerId);
    }

    // @author Christoffer
    private void commandRecordPaymentCompleted(Event e) {
        String correlationId = e.getArgument(0, String.class);
        PaymentId paymentId = readModelRepository.getPendingPaymentId(correlationId);
        if (paymentId == null) {
            ConcurrentIdState state = pendingIdValidations.get(correlationId);
            if (state != null) {
                paymentId = state.paymentId;
            }
        }
        if (paymentId == null) {
            pendingIdValidations.remove(correlationId);
            return;
        }
        Payment payment = paymentRepository.getById(paymentId);
        payment.markCompleted(correlationId);
        paymentRepository.save(payment);
        pendingIdValidations.remove(correlationId);
    }

    // @author Peter
    private void commandCreatePayment(String correlationId, int amount, String merchantId, String token) {
        PaymentId paymentId;
        try {
            Payment payment = Payment.initiate(correlationId, token, merchantId, null, amount);
            paymentRepository.save(payment);
            paymentId = payment.getPaymentId();
            ConcurrentIdState state = pendingIdValidations.get(correlationId);
            if (state != null && state.customerId != null) {
                payment.assignCustomer(AccountId.fromString(state.customerId));
                paymentRepository.save(payment);
            }
        } catch (IllegalArgumentException ex) {
            queue.publish(new Event("PaymentFailed", correlationId, ex.getMessage()));
            queue.publish(new Event("ReleaseTokenRequested", correlationId));
            return;
        }

        updatePaymentState(correlationId, state -> {
            state.hasAmount = true;
            state.amount = amount;
            state.paymentId = paymentId;
        });
    }

}
