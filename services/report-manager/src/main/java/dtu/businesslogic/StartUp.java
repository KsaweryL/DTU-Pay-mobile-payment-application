package dtu.businesslogic;

import messaging.implementations.RabbitMqQueue;

// @author Peter
public class StartUp {
    public static void main(String[] args) throws Exception {
        new StartUp().startUp();
    }

    private void startUp() throws Exception {
        System.out.println("Starting Report Manager Service...");
        var mq = new RabbitMqQueue();
        new ReportManagerService(mq);
        System.out.println("Report Manager Service started and listening to events.");
        
        // Keep the main thread alive
        synchronized (this) {
             this.wait();
        }
    }
}
