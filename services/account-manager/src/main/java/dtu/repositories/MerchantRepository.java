package dtu.repositories;

import org.jmolecules.ddd.annotation.Repository;

import dtu.aggregate.AccountId;
import dtu.aggregate.Merchant;
import messaging.MessageQueue;

// @author Christoffer
@Repository
public class MerchantRepository {
	
	private EventStore eventStore;

	public MerchantRepository(MessageQueue bus) {
		eventStore = new EventStore(bus);
	}

	public Merchant getById(AccountId accountId) {
		return Merchant.createFromEvents(eventStore.getEventsFor(accountId));
	}
	
	public void save(Merchant merchant) {
		eventStore.addEvents(merchant.getAccountId(),merchant.getAppliedEvents());
		merchant.clearAppliedEvents();
	}
}
