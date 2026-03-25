package dtu.events;

import dtu.aggregate.AccountId;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Ksawery
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class MerchantDeregistered extends Event {

    private String correlationId;

    private AccountId accountId;

    public MerchantDeregistered(String correlationId, AccountId accountId) {
        super("MerchantDeregistered", correlationId);
        this.correlationId = correlationId;
        this.accountId = accountId;
    }
}
