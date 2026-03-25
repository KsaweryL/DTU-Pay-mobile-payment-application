package dtu.repositories;

import org.jmolecules.ddd.annotation.Repository;

import dtu.aggregate.AccountId;
import dtu.aggregate.Customer;
import messaging.MessageQueue;


@Repository
// @author Christoffer
public class CustomerRepository {
	
	private EventStore eventStore;

	public CustomerRepository(MessageQueue bus) {
		eventStore = new EventStore(bus);
	}

	public Customer getById(AccountId accountId) {
		return Customer.createFromEvents(eventStore.getEventsFor(accountId));
	}
	
	public void save(Customer customer) {
		eventStore.addEvents(customer.getAccountId(), customer.getAppliedEvents());
		customer.clearAppliedEvents();
	}
}
