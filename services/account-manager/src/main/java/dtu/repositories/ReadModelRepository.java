package dtu.repositories;

import java.util.HashMap;
import java.util.Map;

import org.jmolecules.ddd.annotation.Repository;
import dtu.aggregate.AccountId;
import dtu.aggregate.BankAccount;
import dtu.events.CustomerCreated;
import dtu.events.CustomerDeregistered;
import dtu.events.MerchantCreated;
import dtu.events.MerchantDeregistered;
import messaging.MessageQueue;

// @author Frederik
@Repository
public class ReadModelRepository {

	private Map<AccountId, BankAccount> bankAccounts = new HashMap<>();
	private Map<String, AccountId> CprToAccountId = new HashMap<>();
	private Map<AccountId, String> AccountIdToCpr = new HashMap<>();

	public ReadModelRepository(MessageQueue eventQueue) {
		eventQueue.addHandler("CustomerCreated", this::commandApplyCustomerCreated, CustomerCreated.class);
		eventQueue.addHandler("CustomerDeregistered", this::commandApplyCustomerDeregistered, CustomerDeregistered.class);
        eventQueue.addHandler("MerchantCreated", this::commandApplyMerchantCreated, MerchantCreated.class);
		eventQueue.addHandler("MerchantDeregistered", this::commandApplyMerchantDeregistered, MerchantDeregistered.class);
	}

	public String getBankIdForUser(AccountId accountId) {
		if (accountId == null) return null;
        BankAccount bankAccount = bankAccounts.get(accountId);
        return bankAccount != null ? bankAccount.getId() : null;
    }

	public AccountId getAccountIdFromCpr(String cpr) {
        AccountId accountId = CprToAccountId.get(cpr);
		return accountId;
    }

	public AccountId getMerchantAccountIdFromCpr(String cpr) {
		AccountId accountId = CprToAccountId.get(cpr);
		return accountId;
	}

	public void commandApplyCustomerCreated(CustomerCreated event) {	
		bankAccounts.put(event.getAccountId(), event.getBankAccount());
		CprToAccountId.put(event.getCpr(), event.getAccountId());
		AccountIdToCpr.put(event.getAccountId(), event.getCpr());
	}

	public void commandApplyCustomerDeregistered(CustomerDeregistered event) {
		bankAccounts.remove(event.getAccountId());
		String cpr = AccountIdToCpr.remove(event.getAccountId());
		if (cpr != null) {
			CprToAccountId.remove(cpr);
		}
	}

	public void commandApplyMerchantCreated(MerchantCreated event) {
		bankAccounts.put(event.getAccountId(), event.getBankAccount());
		CprToAccountId.put(event.getCpr(), event.getAccountId());
		AccountIdToCpr.put(event.getAccountId(), event.getCpr());
	}

	public void commandApplyMerchantDeregistered(MerchantDeregistered event) {
		bankAccounts.remove(event.getAccountId());
		String cpr = AccountIdToCpr.remove(event.getAccountId());
		if (cpr != null) {
			CprToAccountId.remove(cpr);
		}
	}
}
