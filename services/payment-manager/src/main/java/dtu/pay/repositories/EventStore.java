package dtu.pay.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import dtu.pay.aggregate.PaymentId;
import lombok.NonNull;
import messaging.Event;
import messaging.MessageQueue;

// @author Peter
public class EventStore {

    private Map<PaymentId, List<Event>> store = new ConcurrentHashMap<>();
    private MessageQueue eventBus;

    public EventStore(MessageQueue bus) {
        this.eventBus = bus;
    }

    public void addEvent(PaymentId paymentId, Event event) {
        if (!store.containsKey(paymentId)) {
            store.put(paymentId, new ArrayList<Event>());
        }
        store.get(paymentId).add(event);
        eventBus.publish(event);
    }

    public Stream<Event> getEventsFor(PaymentId paymentId) {
        if (!store.containsKey(paymentId)) {
            store.put(paymentId, new ArrayList<Event>());
        }
        return store.get(paymentId).stream();
    }

    public void addEvents(@NonNull PaymentId paymentId, List<Event> appliedEvents) {
        appliedEvents.stream().forEach(event -> addEvent(paymentId, event));
    }
}
