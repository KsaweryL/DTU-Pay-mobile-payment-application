package messaging.implementations;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import messaging.Event;
import messaging.MessageQueue;
import utilities.Utils;

public class RabbitMqQueue implements MessageQueue {

	private static final String DEFAULT_HOSTNAME = envOrDefault("RABBITMQ_HOST", "localhost");
	private static final int DEFAULT_PORT = Integer.parseInt(envOrDefault("RABBITMQ_PORT", "5672"));
	private static final String DEFAULT_USERNAME = envOrDefault("RABBITMQ_USER", "guest");
	private static final String DEFAULT_PASSWORD = envOrDefault("RABBITMQ_PASS", "guest");
	private static final String EXCHANGE_NAME = "eventsExchange";
	private static final String QUEUE_TYPE = "topic";

	private Channel channel;
	private String hostname;
	private int port;
	private String username;
	private String password;

	public RabbitMqQueue() {
		this(DEFAULT_HOSTNAME);
	}

	public RabbitMqQueue(String hostname) {
		this.hostname = hostname;
		this.port = DEFAULT_PORT;
		this.username = DEFAULT_USERNAME;
		this.password = DEFAULT_PASSWORD;
		channel = setUpChannel();
	}

	public RabbitMqQueue(String hostname, int port, String username, String password) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		channel = setUpChannel();
	}

	@Override
	public synchronized void publish(Event event) {
		Utils.logPublish(event);
//		System.out.format("[x] publish(%s)\n", event);
		String message = new Gson().toJson(event);
		try {
			channel.basicPublish(EXCHANGE_NAME, event.getTopic(), null, message.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	// Default implementation uses generic Event class
    @Override
    public void addHandler(String topic, Consumer<Event> handler) {
        addHandler(topic, handler, Event.class);
    }

    // New Typed implementation
    @Override
    public <T extends Event> void addHandler(String topic, Consumer<T> handler, Class<T> type) {
        Utils.logAddHandler(topic);
        var chan = setUpChannel();
        try {
            String queueName = chan.queueDeclare().getQueue();
            chan.queueBind(queueName, EXCHANGE_NAME, topic);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                // Deserialize specifically to the target class
                T event = new Gson().fromJson(message, type);
                Utils.logHandle(event);
                handler.accept(event);
            };
            chan.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e1) {
            throw new Error(e1);
        }
    }

	private Channel setUpChannel() {
		Channel chan;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(hostname);
			factory.setPort(port);
			factory.setUsername(username);
			factory.setPassword(password);
			Connection connection = factory.newConnection();
			chan = connection.createChannel();
			chan.exchangeDeclare(EXCHANGE_NAME, QUEUE_TYPE);
		} catch (IOException | TimeoutException e) {
			throw new Error(e);
		}
		return chan;
	}

	private static String envOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		return (value == null || value.isBlank()) ? defaultValue : value;
	}

	// @Override
	// public void addHandler(String topic, Consumer<Event> handler) {
	// 	Utils.logAddHandler(topic);
	// 	var chan = setUpChannel();
	// 	try {
	// 		String queueName = chan.queueDeclare().getQueue();
	// 		chan.queueBind(queueName, EXCHANGE_NAME, topic);

	// 		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
	// 			String message = new String(delivery.getBody(), "UTF-8");

	// 			Event event = new Gson().fromJson(message, Event.class);
	// 			Utils.logHandle(event);
	// 			handler.accept(event);
	// 		};
	// 		chan.basicConsume(queueName, true, deliverCallback, consumerTag -> {
	// 		});
	// 	} catch (IOException e1) {
	// 		throw new Error(e1);
	// 	}
	// }

}
