package dtu.businesslogic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import messaging.implementations.MessageQueueSync;
import org.junit.Test;

public class TokenManagerServiceTest {

    @Test
    public void createTokensShouldReturnRequestedCountAndBeActive() {
        var queue = new MessageQueueSync();
        var service = new TokenManagerService(queue);
        UUID customerId = UUID.randomUUID();

        List<String> tokens = service.createTokens(customerId, 3);

        assertEquals(3, tokens.size());
        assertTrue(service.hasActiveTokens(customerId));
        assertEquals(3, service.getTokens(customerId).size());
    }

    @Test
    public void reserveReleaseConsumeFlowUpdatesStatus() {
        var queue = new MessageQueueSync();
        var service = new TokenManagerService(queue);
        UUID customerId = UUID.randomUUID();

        List<String> tokens = service.createTokens(customerId, 1);
        String token = tokens.get(0);

        Optional<UUID> owner = service.reserveToken(token);
        assertTrue(owner.isPresent());
        assertEquals(customerId, owner.get());
        assertTrue(service.releaseToken(token));

        Optional<UUID> reservedAgain = service.reserveToken(token);
        assertTrue(reservedAgain.isPresent());
        assertTrue(service.consumeToken(token));
        assertFalse(service.hasActiveTokens(customerId));
    }

    @Test
    public void invalidateTokensForCustomerMarksTokensInvalidated() {
        var queue = new MessageQueueSync();
        var service = new TokenManagerService(queue);
        UUID customerId = UUID.randomUUID();

        List<String> tokens = service.createTokens(customerId, 2);
        service.invalidateTokensForCustomer(customerId);

        for (String token : tokens) {
            assertTrue(service.isInvalidated(token));
        }
        assertFalse(service.hasActiveTokens(customerId));
    }
}
