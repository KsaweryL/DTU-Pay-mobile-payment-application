package dtu.businesslogic;
import dtu.TokenService;
import messaging.implementations.RabbitMqQueue;

// @author Tobias
public class StartUp {

    public static void main(String[] args) throws Exception {
        new StartUp().start();
    }

    private void start() throws InterruptedException {
        System.out.println("Starting Token Manager...");
        var queue = new RabbitMqQueue();
        new TokenService(queue, new TokenManagerService(queue));
        System.out.println("Token Manager ready and listening for events.");

        synchronized (this) {
            this.wait();
        }
    }
}
