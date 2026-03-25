package dtu.pay;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.junit.Test;

import messaging.Event;
import messaging.MessageQueue;

public class PaymentReadModelReportTest {

    @Test
    public void merchantReportUsesReadModel() {
        TestMessageQueue queue = new TestMessageQueue();
        PaymentService service = new PaymentService(queue);

        String correlationId = "corr-1";
        String merchantId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String token = "token-1";
        int amount = 100;

        queue.publish(new Event("PaymentRequest", correlationId, amount, merchantId, token));
        queue.publish(new Event("TokenValidated", correlationId, customerId));
        queue.publish(new Event("PaymentCompleted", correlationId));

        queue.publish(new Event("GetMerchantReportRequested", correlationId, merchantId));
        Event reportEvent = queue.lastEvent("MerchantFullReportReceived");
        assertNotNull(reportEvent);
        String reportJson = reportEvent.getArgument(1, String.class);
        assertTrue(reportJson.contains(token));
        assertTrue(reportJson.contains(merchantId));
    }

    private static final class TestMessageQueue implements MessageQueue {
        private final Map<String, List<Consumer<Event>>> handlers = new ConcurrentHashMap<>();
        private final List<Event> publishedEvents = new CopyOnWriteArrayList<>();

        @Override
        public void publish(Event event) {
            publishedEvents.add(event);
            List<Consumer<Event>> topicHandlers = handlers.get(event.getTopic());
            if (topicHandlers == null) {
                return;
            }
            for (Consumer<Event> handler : topicHandlers) {
                handler.accept(event);
            }
        }

        @Override
        public void addHandler(String topic, Consumer<Event> handler) {
            handlers.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>()).add(handler);
        }

        @Override
        public <T extends Event> void addHandler(String topic, Consumer<T> handler, Class<T> type) {
            addHandler(topic, event -> handler.accept(type.cast(event)));
        }

        public Event lastEvent(String topic) {
            Event last = null;
            for (Event event : publishedEvents) {
                if (topic.equals(event.getTopic())) {
                    last = event;
                }
            }
            return last;
        }
    }
}
