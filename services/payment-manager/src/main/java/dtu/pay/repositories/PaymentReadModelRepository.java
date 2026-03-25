package dtu.pay.repositories;

import java.util.concurrent.ConcurrentHashMap;

import dtu.pay.aggregate.AccountId;
import dtu.pay.aggregate.Payment;
import dtu.pay.aggregate.PaymentId;
import dtu.pay.events.PaymentFailedRecorded;
import dtu.pay.events.PaymentInitiated;
import dtu.pay.events.PaymentRecorded;
import messaging.MessageQueue;

// @author Fabian
public class PaymentReadModelRepository {

    private final ConcurrentHashMap<String, Payment> payments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PaymentId> pendingPayments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PaymentId, String> pendingCorrelations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> pendingTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PaymentId, String> pendingTokensByPaymentId = new ConcurrentHashMap<>();

    public PaymentReadModelRepository(MessageQueue bus) {
        bus.addHandler("PaymentInitiated", this::commandApplyPaymentInitiated, PaymentInitiated.class);
        bus.addHandler("PaymentRecorded", this::commandApplyPaymentRecorded, PaymentRecorded.class);
        bus.addHandler("PaymentFailedRecorded", this::commandApplyPaymentFailedRecorded, PaymentFailedRecorded.class);
    }

    public ConcurrentHashMap<String, Payment> getPayments() {
        return payments;
    }

    public ConcurrentHashMap<String, Payment> getPaymentsByCustomer(String customerId) {
        ConcurrentHashMap<String, Payment> customerPayments = new ConcurrentHashMap<>();
        for (var entry : payments.entrySet()) {
            if (matchesAccount(entry.getValue().getCustomerId(), customerId)) {
                customerPayments.put(entry.getKey(), entry.getValue());
            }
        }
        return customerPayments;
    }

    public ConcurrentHashMap<String, Payment> getPaymentsByMerchant(String merchantId) {
        ConcurrentHashMap<String, Payment> merchantPayments = new ConcurrentHashMap<>();
        for (var entry : payments.entrySet()) {
            if (matchesAccount(entry.getValue().getMerchantId(), merchantId)) {
                merchantPayments.put(entry.getKey(), entry.getValue());
            }
        }
        return merchantPayments;
    }

    public String getSerializedPayments() {
        try {
            return serializePayments(payments);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to serialize payments", ex);
        }
    }

    public String getSerializedPaymentsByCustomer(String customerId) {
        try {
            return serializePayments(getPaymentsByCustomer(customerId));
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to serialize customer payments", ex);
        }
    }

    public String getSerializedPaymentsByMerchant(String merchantId) {
        try {
            return serializePayments(getPaymentsByMerchant(merchantId));
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to serialize merchant payments", ex);
        }
    }

    public PaymentId getPendingPaymentId(String correlationId) {
        if (correlationId == null) {
            return null;
        }
        return pendingPayments.get(correlationId);
    }

    private void commandApplyPaymentInitiated(PaymentInitiated event) {
        String correlationId = event.getCorrelationId();
        if (correlationId == null) {
            return;
        }
        pendingPayments.put(correlationId, event.getPaymentId());
        pendingCorrelations.put(event.getPaymentId(), correlationId);
        pendingTokens.put(correlationId, event.getToken());
        pendingTokensByPaymentId.put(event.getPaymentId(), event.getToken());
    }

    private void commandApplyPaymentRecorded(PaymentRecorded event) {
        payments.put(event.getReceiptId(), Payment.fromRecordedEvent(event));
        clearPending(event.getPaymentId());
    }

    private void commandApplyPaymentFailedRecorded(PaymentFailedRecorded event) {
        clearPending(event.getPaymentId());
    }

    private void clearPending(PaymentId paymentId) {
        String correlationId = pendingCorrelations.remove(paymentId);
        if (correlationId != null) {
            pendingPayments.remove(correlationId);
            pendingTokens.remove(correlationId);
        }
        pendingTokensByPaymentId.remove(paymentId);
    }

        private String serializePayments(ConcurrentHashMap<String, Payment> payments) {
        StringBuilder json = new StringBuilder();
        json.append("{\"payments\":[");
        boolean first = true;
        for (var entry : payments.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            Payment payment = entry.getValue();
            json.append('{')
                    .append("\"receiptId\":\"").append(escapeJson(entry.getKey())).append("\",")
                    .append("\"token\":\"").append(escapeJson(payment.getToken())).append("\",")
                    .append("\"merchantId\":\"").append(escapeJson(accountIdToString(payment.getMerchantId()))).append("\",")
                    .append("\"customerId\":\"").append(escapeJson(accountIdToString(payment.getCustomerId()))).append("\",")
                    .append("\"amount\":").append(payment.getAmount())
                    .append('}');
        }
        json.append("]}");
        return json.toString();
    }


    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private boolean matchesAccount(AccountId accountId, String rawId) {
        if (accountId == null || rawId == null) {
            return false;
        }
        return rawId.equals(accountId.getUuid().toString());
    }

        private String accountIdToString(AccountId accountId) {
        if (accountId == null) {
            return "";
        }
        return accountId.getUuid().toString();
    }
}
