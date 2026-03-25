package dtu.events;

import dtu.aggregate.AccountId;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Nikolaj
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class CustomerDeregistered extends Event {
  
    private String correlationId;

    private AccountId accountId;

    public CustomerDeregistered(String correlationId, AccountId accountId) {
        super("CustomerDeregistered", correlationId);
        this.correlationId = correlationId;
        this.accountId = accountId;
    }
}
