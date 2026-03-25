package dtu.aggregate;

// @author Peter
public class ConcurrentIdState {
    public String merchantId;
    public String customerId;

    public boolean isReady() {
        return merchantId != null && customerId != null;
    }
}
