package messaging.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import messaging.Event;
import messaging.MessageQueue;

/* Make sure that the messages are taken from the queue 
 * in order they were published.
 */
public class MessageQueueAsync implements MessageQueue {

	private Map<String, List<Consumer<Event>>> handlersByTopic = new HashMap<>();
	private final BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
	private Thread notificationThread = null;

	public MessageQueueAsync() {
		notificationThread = new Thread(() -> {
			while (true) {
				Event ev;
				try {
					ev = queue.take();
					executeHandlers(ev);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		});
		notificationThread.start();
	}

	private void executeHandlers(Event event) {
		var handlers = handlersByTopic.getOrDefault(event.getTopic(), new ArrayList<Consumer<Event>>());
		handlers.stream().forEach(h -> h.accept(event));
	}

	@Override
	public void publish(Event event) {
		queue.add(event);
//		executeHandlers(event);	
	}

	@Override
	public void addHandler(String topic, Consumer<Event> handler) {
		if (!handlersByTopic.containsKey(topic)) {
			handlersByTopic.put(topic, new ArrayList<Consumer<Event>>());
		}
		handlersByTopic.get(topic).add(handler);
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public <T extends Event> void addHandler(String topic, Consumer<T> handler, Class<T> type) {
        // For in-memory queues type information is preserved so we just delegate
        addHandler(topic, (Consumer<Event>) handler);
	}
}
