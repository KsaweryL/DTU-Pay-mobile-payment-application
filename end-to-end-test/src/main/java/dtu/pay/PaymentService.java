package dtu.pay;

import dtu.pay.resources.PaymentRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class PaymentService implements AutoCloseable {

    private static final String MERCHANT_BASE_URL = getMerchantBaseUrl();
    private static final String CUSTOMER_BASE_URL = getCustomerBaseUrl();
    public WebTarget merchantApi;
    public WebTarget customerApi;
    private Client client;

    // @author Tobias
    private static String getMerchantBaseUrl() {
        String sharedUrl = System.getenv("DTU_PAY_URL");
        if (sharedUrl != null && !sharedUrl.isEmpty()) {
            return sharedUrl;
        }
        String envUrl = System.getenv("DTU_PAY_MERCHANT_URL");
        return (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:8080";
    }

    // @author Christoffer
    private static String getCustomerBaseUrl() {
        String sharedUrl = System.getenv("DTU_PAY_URL");
        if (sharedUrl != null && !sharedUrl.isEmpty()) {
            return sharedUrl;
        }
        String envUrl = System.getenv("DTU_PAY_CUSTOMER_URL");
        return (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:8081";
    }

    // @author Fabian
    public PaymentService() {
        client = ClientBuilder.newClient();
        merchantApi = client.target(MERCHANT_BASE_URL);
        customerApi = client.target(CUSTOMER_BASE_URL);
    }

    // @author Peter
    public String initiatePayment(PaymentRequest paymentRequest) {
        return merchantApi.path("/merchant/payment")
                .request()
                .post(jakarta.ws.rs.client.Entity.json(paymentRequest), String.class);
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

}
