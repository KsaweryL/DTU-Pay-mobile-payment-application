package dtu.events;

import dtu.aggregate.AccountId;
import dtu.aggregate.BankAccount;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import messaging.Event;

// @author Ksawery
@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class MerchantCreated extends Event {
  
    private String correlationId;
    private AccountId accountId;

    private String firstName;
    private String lastName;
    private String cpr;
    private BankAccount bankAccount;

    public MerchantCreated(String correlationId, AccountId accountId, String firstName, String lastName, String cpr, BankAccount bankAccount) {
        super("MerchantCreated", correlationId, accountId);
        this.correlationId = correlationId;
        this.accountId = accountId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cpr = cpr;
        this.bankAccount = bankAccount;
    }
}
