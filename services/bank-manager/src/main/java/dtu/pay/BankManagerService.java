package dtu.pay;

import jakarta.inject.Singleton;
import io.quarkus.runtime.Startup;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;

import java.math.BigDecimal;

@Singleton
@Startup
// @author Ksawery
public class BankManagerService {

    private final MessageQueue queue;

    BankService bank = new BankService_Service().getBankServicePort();;

    public BankManagerService() {
        this(new RabbitMqQueue());
    }

    public BankManagerService(MessageQueue q) {
        queue = q;
        q.addHandler("TransferRequested", this::commandTransferRequested);
    }

    private void commandTransferRequested(Event e) {
        String corrolationID = e.getArgument(0, String.class);
        String merchantBankAccount = e.getArgument(1, String.class);
        String customerBankAccount = e.getArgument(2, String.class);
        Number amountNumber = e.getArgument(3, Number.class);
        int amount = amountNumber.intValue();
        String description = "DTU Pay payment";


        try {
            bank.transferMoneyFromTo(customerBankAccount, merchantBankAccount, BigDecimal.valueOf(amount), description);
            queue.publish(new Event("ConsumeTokenRequested", corrolationID)); // Token service
            queue.publish(new Event("PaymentCompleted", corrolationID)); // Merchant facade
            // queue.publish(new Event("SaveReport", corrolationID)); // Report service - Not caught anywhere

        } catch (BankServiceException_Exception ex) {
            String errorMessage = ex.getMessage();
            queue.publish(new Event("TransferFailed", corrolationID, errorMessage));
            queue.publish(new Event("ReleaseTokenRequested", corrolationID));
            queue.publish(new Event("PaymentFailed", corrolationID, errorMessage));
        }
    }


}
