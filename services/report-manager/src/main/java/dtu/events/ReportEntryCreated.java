package dtu.events;


import dtu.aggregate.AccountId;
import dtu.aggregate.MetaData;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class ReportEntryCreated extends Event {

    private String correlationId;
    MetaData metaData;
    AccountId accountId;

    public ReportEntryCreated(String correlationId, AccountId accountId, MetaData metaData2) {
        super("ReportEntryCreated");
        this.correlationId = correlationId;
        this.accountId = accountId;
        this.metaData = metaData2;
    }

}
