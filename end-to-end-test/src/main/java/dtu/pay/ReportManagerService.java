package dtu.pay;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class ReportManagerService implements AutoCloseable {
    private static final String MERCHANT_BASE_URL = getMerchantBaseUrl();
    private static final String CUSTOMER_BASE_URL = getCustomerBaseUrl();
    private static final String MANAGER_BASE_URL = getManagerBaseUrl();
    public WebTarget merchantApi;
    public WebTarget customerApi;
    public WebTarget managerApi;
    private Client client;

    private static String getMerchantBaseUrl() {
        String sharedUrl = System.getenv("DTU_PAY_URL");
        if (sharedUrl != null && !sharedUrl.isEmpty()) {
            return sharedUrl;
        }
        String envUrl = System.getenv("DTU_PAY_MERCHANT_URL");
        return (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:8080";
    }

    private static String getCustomerBaseUrl() {
        String sharedUrl = System.getenv("DTU_PAY_URL");
        if (sharedUrl != null && !sharedUrl.isEmpty()) {
            return sharedUrl;
        }
        String envUrl = System.getenv("DTU_PAY_CUSTOMER_URL");
        return (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:8081";
    }

    private static String getManagerBaseUrl() {
        String sharedUrl = System.getenv("DTU_PAY_URL");
        if (sharedUrl != null && !sharedUrl.isEmpty()) {
            return sharedUrl;
        }
        String envUrl = System.getenv("DTU_PAY_MANAGER_URL");
        return (envUrl != null && !envUrl.isEmpty()) ? envUrl : "http://localhost:8082";
    }

    // @author Ksawery
    public ReportManagerService() {
        client = ClientBuilder.newClient();
        merchantApi = client.target(MERCHANT_BASE_URL);
        customerApi = client.target(CUSTOMER_BASE_URL);
        managerApi = client.target(MANAGER_BASE_URL);
    }

    // @author Tobias
    public String managerPaymentReportRequest() {
        return managerApi.path("/manager/payments")
                .request()
                .get(String.class);
    }

    // @author Peter
    public String managerMerchantReportRequest(String merchantId) {
        return managerApi.path("/manager/payments/merchant/" + merchantId)
                .request()
                .get(String.class);
    }

    // @author Frederik
    public String managerCustomerReportRequest(String customerId) {
        return managerApi.path("/manager/payments/customer/" + customerId)
                .request()
                .get(String.class);
    }

    // @author Tobias
    public String merchantPaymentReportRequest(String merchantId) {
        return merchantApi.path("/merchant/" + merchantId + "/report")
                .request()
                .get(String.class);
    }

    // @author Christoffer 
    public String customerPaymentReportRequest(String customerId) {
        return customerApi.path("/customer/" + customerId + "/report")
                .request()
                .get(String.class);
    }


    // @author Frederik
    public String managerReportHistoryRequest() {
        return managerApi.path("/manager/history")
                .request()
                .get(String.class);
    }

    // @author Nikolaj
    public String managerMerchantReportHistoryRequest(String merchantId) {
        return managerApi.path("/manager/history/merchant/" + merchantId)
                .request()
                .get(String.class);
    }

    // @author Fabian
    public String managerCustomerReportHistoryRequest(String customerId) {
        return managerApi.path("/manager/history/customer/" + customerId)
                .request()
                .get(String.class);
    }

    // @author Ksawery
    public String customerReportHistoryRequest(String customerId) {
        return customerApi.path("/customer/" + customerId + "/report/history")
                .request()
                .get(String.class);
    }
    
    //@author Fabian    
    public String merchantReportHistoryRequest(String merchantId) {
        return merchantApi.path("/merchant/" + merchantId + "/report/history")
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
