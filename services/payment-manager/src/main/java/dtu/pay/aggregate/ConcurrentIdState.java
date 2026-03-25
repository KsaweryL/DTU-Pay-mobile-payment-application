package dtu.pay.aggregate;

// @author ksawery 
public class ConcurrentIdState {
    public String BankMerchantId;
    public String BankCustomerId;
    public String customerId;
    public PaymentId paymentId;
    public boolean hasAmount;
    public int amount;
    public boolean transferRequested;

    public boolean isReady() {
        return BankMerchantId != null && BankCustomerId != null && customerId != null && hasAmount;
    }
    
}
