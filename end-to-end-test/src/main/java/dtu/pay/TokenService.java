package dtu.pay;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;

public class TokenService implements AutoCloseable {
    private static final String BASE_URL = getBaseUrl();
    public WebTarget api;
    private Client client;

    // @author Tobias
    private static String getBaseUrl() {
        String envUrl = System.getenv("DTU_PAY_URL");
        return (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:8081";
    }

    public TokenService() {
        client = ClientBuilder.newClient();
        api = client.target(BASE_URL);
    }

    //@author Christoffer
    public String generateTokensForCustomer(String customerId, int numberOfTokens) {
        return api.path("/customer/" + customerId + "/tokens")
            .queryParam("count", numberOfTokens)
            .request()
            .put(Entity.json("{}"), String.class);
    }

    // @author Frederik
    public String getTokens(String customerId) {
        return api.path("/customer/" + customerId + "/tokens")
                .request()
                .get(String.class);
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

}
