package dtu.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
// import java.util.HashSet;
import java.util.List;
import java.util.Map;
// import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
// import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;

// import com.rabbitmq.client.Address;

import dtu.events.MerchantCreated;
import dtu.events.MerchantDeregistered;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import messaging.Event;

@AggregateRoot
@Entity
@Getter
public class Merchant {
	private AccountId accountId;
	private String firstName;
	private String lastName;
	private String cpr;
	private BankAccount bankAccount;
	
	@Setter(AccessLevel.NONE)
	private List<Event> appliedEvents = new ArrayList<Event>();

	private Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();

	// @author Tobias
	public static Merchant create(String correlationId, String firstName, String lastName, String cpr, String bankId) {
    AccountId accountId = new AccountId(UUID.randomUUID());
    MerchantCreated event = new MerchantCreated(correlationId, accountId, firstName, lastName, cpr, new BankAccount(bankId));
    Merchant merchant = new Merchant();
    merchant.apply(event); 
    merchant.appliedEvents.add(event);
    return merchant;
	}

	// @author Tobias
	public void delete(String correlationId) {
		MerchantDeregistered event = new MerchantDeregistered(correlationId, this.accountId);
		this.appliedEvents.add(event);
	}

	// @author Christoffer
	public static Merchant createFromEvents(Stream<Event> events) {
		Merchant merchant = new Merchant();
		merchant.applyEvents(events);
		return merchant;
	}

	public Merchant() {
		registerEventHandlers();
	}

	// @author Christoffer
	private void registerEventHandlers() {
		handlers.put(MerchantCreated.class, e -> apply((MerchantCreated) e));
	}

	/* Business Logic */
	
	/* Event Handling */

	// @author Ksawery
	private void applyEvents(Stream<Event> events) throws Error {
		events.forEachOrdered(e -> {
			this.applyEvent(e);
		});
		if (this.getAccountId() == null) {
			throw new Error("merchant does not exist");
		}
	}

	// @author Ksawery
	private void applyEvent(Event e) {
		handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
	}

	// @author Fabian
	private void missingHandler(Event e) {
		throw new Error("handler for event "+e+" missing");
	}

	// @author Tobias
	private void apply(MerchantCreated event) {
		accountId = event.getAccountId();
		firstName = event.getFirstName();
		lastName = event.getLastName();
		cpr = event.getCpr();
		bankAccount = event.getBankAccount();
	}


	// @author Tobias
	public void clearAppliedEvents() {
		appliedEvents.clear();
	}

}
