package dtu.pay;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Test;
import messaging.Event;
import messaging.MessageQueue;

public class PaymentConcurrencyTest {

    @Test
    public void transferRequestedHandled()
            throws Exception {
        System.setProperty("dtu.concurrency.test.delay.ms", "10");
        try {
            ConcurrentMessageQueue queue = new ConcurrentMessageQueue();
            new PaymentService(queue);

            String correlationId = "corr-1";
            String merchantBankAccount = "merchant-bank-1";
            String customerBankAccount = "customer-bank-1";
            int amount = 100;
            String merchantId = UUID.randomUUID().toString();
            String token = "token-1";
            String customerId = UUID.randomUUID().toString();

            Event paymentRequest = new Event("PaymentRequest", correlationId, amount, merchantId, token);
            Event bankAccountDetailsProvided = new Event(
                    "BankAccountDetailsProvided",
                    correlationId,
                    merchantBankAccount,
                    customerBankAccount);
            Event tokenValidated = new Event("TokenValidated", correlationId, customerId);

            ExecutorService executor = Executors.newFixedThreadPool(3);
            CountDownLatch ready = new CountDownLatch(3);
            CountDownLatch start = new CountDownLatch(1);

            Runnable paymentRequestTask = () -> {
                ready.countDown();
                await(start);
                queue.publish(paymentRequest);
            };

            Runnable bankDetailsTask = () -> {
                ready.countDown();
                await(start);
                queue.publish(bankAccountDetailsProvided);
            };

            Runnable tokenValidatedTask = () -> {
                ready.countDown();
                await(start);
                queue.publish(tokenValidated);
            };

            executor.submit(paymentRequestTask);
            executor.submit(bankDetailsTask);
            executor.submit(tokenValidatedTask);

            ready.await(1, TimeUnit.SECONDS);
            start.countDown();

            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            assertEquals(1, queue.countTopic("TransferRequested"));
        } finally {
            System.clearProperty("dtu.concurrency.test.delay.ms");
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /* Concurrent message queue for testing */
    private static final class ConcurrentMessageQueue implements MessageQueue {
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

        public long countTopic(String topic) {
            return publishedEvents.stream().filter(event -> event.getTopic().equals(topic)).count();
        }
    }
}
