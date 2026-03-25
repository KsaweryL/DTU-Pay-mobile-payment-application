package dtu.businesslogic;

import messaging.implementations.RabbitMqQueue;

// @author ksawery 
public class StartUp {
    public static void main(String[] args) throws Exception {
        new StartUp().startUp();
    }

    private void startUp() throws Exception {
        System.out.println("Starting Account Creation Service...");
        var mq = new RabbitMqQueue();
        new AccountManagerService(mq);
        System.out.println("Account Creation Service started and listening to events.");
        
        // Keep the main thread alive
        synchronized (this) {
             this.wait();
        }
    }
}
