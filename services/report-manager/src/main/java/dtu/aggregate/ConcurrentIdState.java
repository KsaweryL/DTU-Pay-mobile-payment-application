package dtu.aggregate;

// @author Ksawery
public class ConcurrentIdState {
    public String merchantID;
    public String customerID;
    public boolean hasAmount;
    public int amount;
    public String token;
    
    public boolean isReady() {
        return merchantID != null && customerID != null && hasAmount && token != null;
    }
    
}
