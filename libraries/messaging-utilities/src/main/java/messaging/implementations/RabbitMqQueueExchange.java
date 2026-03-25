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

public class RabbitMqQueueExchange implements MessageQueue {

	private static final String DEFAULT_HOSTNAME = envOrDefault("RABBITMQ_HOST", "localhost");
	private static final int DEFAULT_PORT = Integer.parseInt(envOrDefault("RABBITMQ_PORT", "5672"));
	private static final String DEFAULT_USERNAME = envOrDefault("RABBITMQ_USER", "guest");
	private static final String DEFAULT_PASSWORD = envOrDefault("RABBITMQ_PASS", "guest");
	private static final String QUEUE_TYPE = "topic";

//	private Channel channel;
	private String hostname;
	private int port;
	private String username;
	private String password;

	public RabbitMqQueueExchange() {
		this(DEFAULT_HOSTNAME);
	}

	public RabbitMqQueueExchange(String hostname) {
		this.hostname = hostname;
		this.port = DEFAULT_PORT;
		this.username = DEFAULT_USERNAME;
		this.password = DEFAULT_PASSWORD;
//		channel = setUpChannel();
	}

	public RabbitMqQueueExchange(String hostname, int port, String username, String password) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	@Override
	public void publish(Event event) {
		System.out.format("[x] publish(%s)\n", event);
		String message = new Gson().toJson(event);
		var channel = setUpChannel(event.getTopic());
		try {
			channel.basicPublish(event.getTopic(), "", null, message.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private Channel setUpChannel(String topic) {
		Channel chan;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(hostname);
			factory.setPort(port);
			factory.setUsername(username);
			factory.setPassword(password);
			Connection connection = factory.newConnection();
			chan = connection.createChannel();
			chan.exchangeDeclare(topic, QUEUE_TYPE,true);
		} catch (IOException | TimeoutException e) {
			throw new Error(e);
		}
		return chan;
	}

	private static String envOrDefault(String key, String defaultValue) {
		String value = System.getenv(key);
		return (value == null || value.isBlank()) ? defaultValue : value;
	}

	@Override
	public void addHandler(String topic, Consumer<Event> handler) {
		System.out.format("[x] addHandler(%s)\n", topic);
		var chan = setUpChannel(topic);
		try {
			String queueName = chan.queueDeclare().getQueue();
			chan.queueBind(queueName, topic, "#");

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), "UTF-8");

				Event event = new Gson().fromJson(message, Event.class);
				System.out.format("[x] executingHandler(%s)\n", event);
				handler.accept(event);
			};
			chan.basicConsume(queueName, true, deliverCallback, consumerTag -> {
			});
		} catch (IOException e1) {
			throw new Error(e1);
		}
	}

	// @Override
    // public void addHandler(String topic, Consumer<Event> handler) {
    //     addHandler(topic, handler, Event.class);
    // }

    @Override
    public <T extends Event> void addHandler(String topic, Consumer<T> handler, Class<T> type) {
        System.out.format("[x] addHandler(%s)\n", topic);
        var chan = setUpChannel(topic);
        try {
            String queueName = chan.queueDeclare().getQueue();
            chan.queueBind(queueName, topic, "#");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                T event = new Gson().fromJson(message, type);
                System.out.format("[x] executingHandler(%s)\n", event);
                handler.accept(event);
            };
            chan.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e1) {
            throw new Error(e1);
        }
    }

}
