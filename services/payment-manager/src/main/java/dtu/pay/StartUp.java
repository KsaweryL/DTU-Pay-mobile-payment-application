package dtu.pay;

import messaging.implementations.RabbitMqQueue;

// @author Fabian
public class StartUp {
    public static void main(String[] args) throws Exception {
        new StartUp().startUp();
    }

    private void startUp() throws Exception {
        System.out.println("Starting Payment Service...");
        var mq = new RabbitMqQueue();
        new PaymentService(mq);
        System.out.println("Payment Service started and listening to events.");
        
        // Keep the main thread alive
        synchronized (this) {
             this.wait();
        }
    }
}
