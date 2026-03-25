package dtu;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dtu.businesslogic.TokenManagerService;
import messaging.Event;
import messaging.MessageQueue;

public class TokenService {
    private final MessageQueue queue;
    private final TokenManagerService repository;
    private ConcurrentHashMap<String, String> pendingTokenConsumption = new ConcurrentHashMap<>();

    public TokenService(MessageQueue queue, TokenManagerService repository) {
        this.queue = queue;
        this.repository = repository;

        queue.addHandler("RequestPaymentTokens", this::policyRequestPaymentTokens);
        queue.addHandler("CustomerDeRegistrationRequested", this::commandCustomerDeregisteredRequest);
        queue.addHandler("ValidateTokenRequest", this::policyValidateTokenRequest);
        queue.addHandler("ConsumeTokenRequested", this::commandConsumeTokenRequested);
        queue.addHandler("ReleaseTokenRequested", this::commandReleaseTokenRequested);
    }

    // @author Ksawery
    private void policyRequestPaymentTokens(Event event) {
        String correlationId = event.getArgument(0, String.class);
        var customerId = parseCustomerId(event.getArgument(1, String.class));
        int requested = event.getArgument(2, Integer.class);

        if (requested <= 0) {
            publishRequestRejected(correlationId, customerId, "Number of tokens must be positive");
            return;
        }
        if (requested > 5) {
            publishRequestRejected(correlationId, customerId, "Cannot request more than 5 tokens at a time");
            return;
        }
        if (repository.hasActiveTokens(customerId)) {
            publishRequestRejected(correlationId, customerId,
                    "Cannot request more tokens until existing tokens are used");
            return;
        }

        commandCreateTokens(correlationId, customerId, requested);
    }

    // @author Peter
    private void commandCreateTokens(String correlationId, UUID customerId, int requested) {
        List<String> tokens = repository.createTokens(customerId, requested);
        queue.publish(new Event("PaymentTokensGenerated", correlationId, customerId.toString(),
                tokens.toArray(new String[0])));
    }

    // @author Ksawery
    private void policyValidateTokenRequest(Event event) {
        String correlationId = event.getArgument(0, String.class);
        String token = event.getArgument(1, String.class);

        if (token == null || token.isBlank() || "null".equalsIgnoreCase(token)) {
            queue.publish(new Event("TokenInvalid", correlationId, "Customer has no tokens available"));
            queue.publish(new Event("ReleaseTokenRequested", correlationId));
            queue.publish(new Event("PaymentFailed", correlationId, "Customer has no tokens available"));
            return;
        }

        if (repository.isInvalidated(token)) {
            queue.publish(new Event("TokenInvalid", correlationId, "Invalid token used for payment"));
            queue.publish(new Event("ReleaseTokenRequested", correlationId));
            queue.publish(new Event("PaymentFailed", correlationId, "Invalid token used for payment"));
            return;
        }

        Optional<UUID> owner = repository.reserveToken(token);
        if (owner.isEmpty()) {
            queue.publish(new Event("TokenInvalid", correlationId, "Token is invalid or already used"));
            queue.publish(new Event("ReleaseTokenRequested", correlationId));
            queue.publish(new Event("PaymentFailed", correlationId, "Token is invalid or already used"));
            return;
        }

        commandReserveToken(correlationId, token, owner);
    }

    // @author Christoffer
    private void commandReserveToken(String correlationId, String token, Optional<UUID> owner) {
        try {

            pendingTokenConsumption.put(correlationId, token);

            String customerId = owner.get().toString();
            queue.publish(new Event("TokenValidated", correlationId, customerId));
            queue.publish(new Event("ValidateCidRequest", correlationId, customerId));
            // queue.publish(new Event("ReportCidRequest", correlationId, customerId)); -
            // Not caught anywhere

        } catch (RuntimeException ex) {
            queue.publish(new Event("TokenInvalid", correlationId, "Token is invalid or already used"));
            queue.publish(new Event("ReleaseTokenRequested", correlationId));
            queue.publish(new Event("PaymentFailed", correlationId, "Token is invalid or already used"));
        }
    }

    // @author Frederik
    private void commandConsumeTokenRequested(Event event) {
        String correlationId = event.getArgument(0, String.class);
        String token = pendingTokenConsumption.remove(correlationId);
        if (token != null) {
            repository.consumeToken(token);
        }
    }

    // @author Ksawery
    private void commandReleaseTokenRequested(Event event) {
        String token = event.getArgument(0, String.class);
        String tokenFromCorrelation = pendingTokenConsumption.remove(token);
        if (tokenFromCorrelation != null) {
            token = tokenFromCorrelation;
        }
        repository.releaseToken(token);
    }

    // @author Ksawery
    private void commandCustomerDeregisteredRequest(Event event) {
        String customerId = event.getArgument(1, String.class);
        repository.invalidateTokensForCustomer(parseCustomerId(customerId));
    }

    // @author Ksawery
    private void publishRequestRejected(String correlationId, UUID customerId, String reason) {
        queue.publish(new Event("PaymentTokensRejected", correlationId, customerId.toString(), reason));
    }

    private UUID parseCustomerId(String rawValue) {
        return UUID.fromString(rawValue);
    }
}
