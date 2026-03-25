package dtu.customer_communication;

// @author Peter
public class TokenRequest {

    public String customerId;
    public int numberOfTokens;

    public TokenRequest() {}

    public TokenRequest(String customerId, int numberOfTokens) {
        this.customerId = customerId;
        this.numberOfTokens = numberOfTokens;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getNumberOfTokens() {
        return numberOfTokens;
    }
}

