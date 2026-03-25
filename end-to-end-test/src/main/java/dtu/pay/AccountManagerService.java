package dtu.pay;

import dtu.pay.resources.Customer;
import dtu.pay.resources.Merchant;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import java.util.concurrent.TimeUnit;

public class AccountManagerService implements AutoCloseable {

    private static final String MERCHANT_BASE_URL = getMerchantBaseUrl();
    private static final String CUSTOMER_BASE_URL = getCustomerBaseUrl();
    private static final long CONNECT_TIMEOUT_SECONDS = 5;
    private static final long READ_TIMEOUT_SECONDS = 10;
    public WebTarget merchantApi;
    public WebTarget customerApi;
    private Client client;

    public AccountManagerService() {
        client = ClientBuilder.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        merchantApi = client.target(MERCHANT_BASE_URL);
        customerApi = client.target(CUSTOMER_BASE_URL);
    }

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

    // @author Frederik
    public String merchantRegisterRequest(Merchant merchant) {
        return merchantApi.path("/merchant")
                .request()
                .post(jakarta.ws.rs.client.Entity.json(merchant), String.class);
    }

    // @author Ksawery
    public String customerRegisterRequest(Customer customer) {
        return customerApi.path("/customer")
                .request()
                .post(jakarta.ws.rs.client.Entity.json(customer), String.class);
    }

    // @author Fabian
    public String merchantDeregisterRequest(String mid) {
        return merchantApi.path("/merchant/" + mid)
                .request()
                .delete(String.class);

    }

     // @author Nikolaj
    public String customerDeregisterRequest(String cid) {
        return customerApi.path("/customer/" + cid)
                .request()
                .delete(String.class);
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
