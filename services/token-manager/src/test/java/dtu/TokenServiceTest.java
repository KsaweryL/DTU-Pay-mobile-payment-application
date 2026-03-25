package dtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dtu.businesslogic.TokenManagerService;
import messaging.Event;
import messaging.implementations.MessageQueueSync;
import org.junit.Test;

public class TokenServiceTest {

    @Test
    public void requestPaymentTokensPublishesGeneratedEvent() {
        MessageQueueSync queue = new MessageQueueSync();
        TokenManagerService repository = new TokenManagerService(queue);
        new TokenService(queue, repository);

        List<Event> generated = new ArrayList<>();
        queue.addHandler("PaymentTokensGenerated", generated::add);

        String correlationId = UUID.randomUUID().toString();
        UUID customerId = UUID.randomUUID();

        queue.publish(new Event("RequestPaymentTokens", correlationId, customerId.toString(), 3));

        assertEquals(1, generated.size());
        Event event = generated.get(0);
        assertEquals(correlationId, event.getArgument(0, String.class));
        assertEquals(customerId.toString(), event.getArgument(1, String.class));
        String[] tokens = event.getArgument(2, String[].class);
        assertNotNull(tokens);
        assertEquals(3, tokens.length);
        assertTrue(repository.hasActiveTokens(customerId));
    }

    @Test
    public void requestPaymentTokensRejectsTooMany() {
        MessageQueueSync queue = new MessageQueueSync();
        TokenManagerService repository = new TokenManagerService(queue);
        new TokenService(queue, repository);

        List<Event> rejected = new ArrayList<>();
        queue.addHandler("PaymentTokensRejected", rejected::add);

        String correlationId = UUID.randomUUID().toString();
        UUID customerId = UUID.randomUUID();

        queue.publish(new Event("RequestPaymentTokens", correlationId, customerId.toString(), 6));

        assertEquals(1, rejected.size());
        Event event = rejected.get(0);
        assertEquals(correlationId, event.getArgument(0, String.class));
        assertEquals(customerId.toString(), event.getArgument(1, String.class));
        assertTrue(event.getArgument(2, String.class).contains("Cannot request more than 5"));
        assertFalse(repository.hasActiveTokens(customerId));
    }

    @Test
    public void validateAndConsumeTokenPublishesEventsAndConsumes() {
        MessageQueueSync queue = new MessageQueueSync();
        TokenManagerService repository = new TokenManagerService(queue);
        new TokenService(queue, repository);

        UUID customerId = UUID.randomUUID();
        String token = repository.createTokens(customerId, 1).get(0);

        List<Event> validated = new ArrayList<>();
        List<Event> validateCid = new ArrayList<>();
        queue.addHandler("TokenValidated", validated::add);
        queue.addHandler("ValidateCidRequest", validateCid::add);

        String correlationId = UUID.randomUUID().toString();
        queue.publish(new Event("ValidateTokenRequest", correlationId, token));

        assertEquals(1, validated.size());
        assertEquals(1, validateCid.size());
        assertEquals(customerId.toString(), validated.get(0).getArgument(1, String.class));

        queue.publish(new Event("ConsumeTokenRequested", correlationId));
        assertFalse(repository.hasActiveTokens(customerId));
    }
}
