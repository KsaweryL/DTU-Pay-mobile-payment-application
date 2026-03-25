package messaging;

import java.util.function.Consumer;

public interface MessageQueue {

	void publish(Event event);
	void addHandler(String topic, Consumer<Event> handler);
	<T extends Event> void addHandler(String topic, Consumer<T> handler, Class<T> type);


}
