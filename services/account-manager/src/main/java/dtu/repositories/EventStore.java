package dtu.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import lombok.NonNull;
import messaging.Event;
import messaging.MessageQueue;
import dtu.aggregate.AccountId;

// @author Tobias
public class EventStore {

	private Map<AccountId, List<Event>> store = new ConcurrentHashMap<>();

	private MessageQueue eventBus;

	public EventStore(MessageQueue bus) {
		this.eventBus = bus;
	}

	public void addEvent(AccountId accountId, Event event) {
		if (!store.containsKey(accountId)) {
			store.put(accountId, new ArrayList<Event>());
		}
		store.get(accountId).add(event);
		eventBus.publish(event);
	}
	
	public Stream<Event> getEventsFor(AccountId accountId) {
		if (!store.containsKey(accountId)) {
			store.put(accountId, new ArrayList<Event>());
		}
		return store.get(accountId).stream();
	}

	public void addEvents(@NonNull AccountId accountId, List<Event> appliedEvents) {
		appliedEvents.stream().forEach(e -> addEvent(accountId, e));
	}

}
