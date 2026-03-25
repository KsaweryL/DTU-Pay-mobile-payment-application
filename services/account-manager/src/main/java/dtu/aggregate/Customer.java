package dtu.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;
import dtu.events.CustomerCreated;
import dtu.events.CustomerDeregistered;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import messaging.Event;

@AggregateRoot
@Entity
@Getter
public class Customer {
	private AccountId accountId;
	private String firstName;
	private String lastName;
	private String cpr;
	private BankAccount bankAccount;

	@Setter(AccessLevel.NONE)
	private List<Event> appliedEvents = new ArrayList<Event>();

	private Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();

	// @author Nikolaj
	public static Customer create(String correlationId, String firstName, String lastName, String cpr, String bankId) {
		AccountId accountId = new AccountId(UUID.randomUUID());
		CustomerCreated event = new CustomerCreated(correlationId, accountId, firstName, lastName, cpr,
				new BankAccount(bankId));
		Customer customer = new Customer();
		customer.apply(event);
		customer.appliedEvents.add(event);
		return customer;
	}

	// @author Tobias
	public void delete(String correlationId) {
		CustomerDeregistered event = new CustomerDeregistered(correlationId, this.accountId);
		this.appliedEvents.add(event);
	}

	public static Customer createFromEvents(Stream<Event> events) {
		Customer customer = new Customer();
		customer.applyEvents(events);
		return customer;
	}

	public Customer() {
		registerEventHandlers();
	}

	// @author Frederik
	private void registerEventHandlers() {
		handlers.put(CustomerCreated.class, e -> apply((CustomerCreated) e));
	}

	// @author Fabian
	private void applyEvents(Stream<Event> events) throws Error {
		events.forEachOrdered(e -> {
			this.applyEvent(e);
		});
		if (this.getAccountId() == null) {
			throw new Error("user does not exist");
		}
	}

	// @author Frederik
	private void applyEvent(Event e) {
		handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
	}

	// @author Frederik
	private void missingHandler(Event e) {
		throw new Error("handler for event " + e + " missing");
	}

	// @author Peter
	private void apply(CustomerCreated event) {
		accountId = event.getAccountId();
		firstName = event.getFirstName();
		lastName = event.getLastName();
		cpr = event.getCpr();
		bankAccount = event.getBankAccount();
	}

	public void clearAppliedEvents() {
		appliedEvents.clear();
	}

}
