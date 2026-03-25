package dtu.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import dtu.aggregate.TokenId;
import lombok.NonNull;
import messaging.Event;
import messaging.MessageQueue;

// @author Tobias
public class EventStore {
    private Map<TokenId, List<Event>> store = new ConcurrentHashMap<>();
    private MessageQueue eventBus;

    public EventStore(MessageQueue bus) {
        this.eventBus = bus;
    }

    public void addEvent(TokenId tokenId, Event event) {
        if (!store.containsKey(tokenId)) {
            store.put(tokenId, new ArrayList<Event>());
        }
        store.get(tokenId).add(event);
        eventBus.publish(event);
    }

    public Stream<Event> getEventsFor(TokenId tokenId) {
        if (!store.containsKey(tokenId)) {
            store.put(tokenId, new ArrayList<Event>());
        }
        return store.get(tokenId).stream();
    }

    public void addEvents(@NonNull TokenId tokenId, List<Event> appliedEvents) {
        appliedEvents.stream().forEach(event -> addEvent(tokenId, event));
    }
}
