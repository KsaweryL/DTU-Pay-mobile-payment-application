package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import dtu.pay.resources.PaymentRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ReportSteps {
    Context context;

    public ReportSteps(Context context) {
        this.context = context;
    }


    //////////////////////////// GIVEN STEPS ////////////////////////////

    // @author Frederik
    @Given("the customer successfully makes a payment of {int} kr to the merchant")
    public void theCustomerSuccessfullyMakesAPaymentOfKrToTheMerchant(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        String token = context.tokenList.get(context.cid1);
        context.paymentTokenUsed = token;
        PaymentRequest paymentRequest = new PaymentRequest(int1, context.mid1, token);
        context.result = context.paymentService.initiatePayment(paymentRequest);
        assertEquals("Payment completed successfully", context.result);
    }

    //////////////////////////// WHEN STEPS ////////////////////////////

    // @author Ksawery
    @When("the customer requests a report")
    public void theCustomerRequestsAReport() {
        try {
            context.customerReport = context.reportManagerService.customerPaymentReportRequest(context.cid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Fabian
    @When("the merchant requests a report")
    public void theMerchantRequestsAReport() {
        try {
            context.merchantReport = context.reportManagerService.merchantPaymentReportRequest(context.mid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // @author Nikolaj    
    @When("the manager requests a report")
    public void theManagerRequestsAReport() {
        try {
            context.managerReport = context.reportManagerService.managerPaymentReportRequest();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Peter  
    @When("the customer requests a report history")
    public void theCustomerRequestsAReportHistory() {
        try {
            context.customerReportHistory = context.reportManagerService.customerReportHistoryRequest(context.cid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Tobias
    @When("the manager requests a report for the merchant")
    public void theManagerRequestsAReportForTheMerchant() {
        try {
            context.managerMerchantReport = context.reportManagerService.managerMerchantReportRequest(context.mid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Frederik
    @When("the manager requests a report for the customer")
    public void theManagerRequestsAReportForTheCustomer() {
        try {
            context.managerCustomerReport = context.reportManagerService.managerCustomerReportRequest(context.cid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Christoffer
    @When("the manager requests a report history")
    public void theManagerRequestsAReportHistory() {
        try {
            context.managerReportHistory = context.reportManagerService.managerReportHistoryRequest();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Tobias
    @When("the manager requests a report history for the merchant")
    public void theManagerRequestsAReportHistoryForTheMerchant() {
        try {
            context.managerMerchantReportHistory = context.reportManagerService
                    .managerMerchantReportHistoryRequest(context.mid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Fabian
    @When("the manager requests a report history for the customer")
    public void theManagerRequestsAReportHistoryForTheCustomer() {
        try {
            context.managerCustomerReportHistory = context.reportManagerService
                    .managerCustomerReportHistoryRequest(context.cid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Christoffer
    @When("the merchant requests a report history")
    public void theMerchantRequestsAReportHistory() {
        try {
            context.merchantReportHistory = context.reportManagerService.merchantReportHistoryRequest(context.mid1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //////////////////////////// THEN STEPS ////////////////////////////

    // @author Peter
    @Then("the customer receives an empty report showing no payments made")
    public void theCustomerReceivesAnEmptyReportShowingNoPaymentsMade() {
        assertNotNull(context.customerReport);
        String normalized = context.customerReport.replaceAll("\\s+", "");
        assertTrue(normalized.contains("\"payments\":[]") || normalized.contains("No payments found"));
    }

    // @author Tobias
    @Then("the customer receives a report showing the payment of {int} kr to the merchant")
    public void theCustomerReceivesAReportShowingThePaymentOfKrToTheMerchant(Integer int1) {
        assertNotNull(context.customerReport);

        boolean correctAmount = context.customerReport.contains(int1.toString());
        boolean correctMerchant = context.customerReport.contains(context.mid1);
        assertTrue(correctAmount && correctMerchant);
    }

    // @author Peter
    @Then("the merchant receives a report showing the payment of {int} kr from the customer token")
    public void theMerchantReceivesAReportShowingThePaymentOfKrFromTheCustomerToken(Integer int1) {
        assertNotNull(context.merchantReport);

        boolean correctAmount = context.merchantReport.contains(int1.toString());
        boolean correctToken = context.merchantReport.contains(context.paymentTokenUsed);
        assertTrue(correctAmount && correctToken);
    }

    // @author Christoffer
    @Then("the manager receives a report showing all payments made through DTU pay")
    public void theManagerReceivesAReportShowingAllPaymentsMadeThroughDTUPay() {
           assertNotNull(context.managerReport);

        boolean containsPayment = context.managerReport.contains(context.paymentTokenUsed);
        assertTrue(containsPayment);
    }

    // @author Christoffer
    @Then("the customer receives a report history with at least one entry")
    public void theCustomerReceivesAReportHistoryWithAtLeastOneEntry() {
        assertNotNull(context.customerReportHistory);
        assertTrue(hasHistoryEntries(context.customerReportHistory));
    }
    
    // @author Frederik
    @Then("the manager receives a report showing the payment of {int} kr to the merchant")
    public void theManagerReceivesAReportShowingThePaymentOfKrToTheMerchant(Integer int1) {
        context.managerMerchantReport = retryManagerMerchantReport(int1, context.mid1);
        assertNotNull(context.managerMerchantReport);

        boolean correctAmount = context.managerMerchantReport.contains(int1.toString());
        boolean correctMerchant = context.managerMerchantReport.contains(context.mid1);
        assertTrue(correctAmount && correctMerchant);
    }

    // @author Nikolaj
    @Then("the manager receives a report showing the payment of {int} kr from the customer")
    public void theManagerReceivesAReportShowingThePaymentOfKrFromTheCustomer(Integer int1) {
        context.managerCustomerReport = retryManagerCustomerReport(int1, context.cid1);
        assertNotNull(context.managerCustomerReport);

        boolean correctAmount = context.managerCustomerReport.contains(int1.toString());
        boolean correctCustomer = context.managerCustomerReport.contains(context.cid1);
        assertTrue(correctAmount && correctCustomer);
    }

    // @author Frederik
    @Then("the manager receives a report history with at least one entry")
    public void theManagerReceivesAReportHistoryWithAtLeastOneEntry() {
        assertNotNull(context.managerReportHistory);
        assertTrue(hasHistoryEntries(context.managerReportHistory));
    }

    // @author Nikolaj 
    @Then("the manager receives a report history for the merchant")
    public void theManagerReceivesAReportHistoryForTheMerchant() {
        assertNotNull(context.managerMerchantReportHistory);
        assertTrue(hasHistoryEntries(context.managerMerchantReportHistory));
    }

    // @author Peter
    @Then("the manager receives a report history for the customer")
    public void theManagerReceivesAReportHistoryForTheCustomer() {
        assertNotNull(context.managerCustomerReportHistory);
        assertTrue(hasHistoryEntries(context.managerCustomerReportHistory));
    }

    // @author Tobias
    @Then("the merchant receives a report history with at least one entry")
    public void theMerchantReceivesAReportHistoryWithAtLeastOneEntry() {
        assertNotNull(context.merchantReportHistory);
        assertTrue(hasHistoryEntries(context.merchantReportHistory));
    }

    // @author Tobias
    private boolean hasHistoryEntries(String history) {
        if (history == null) {
            return false;
        }
        String normalized = history.replaceAll("\\s+", "");
        return normalized.contains("\"history\":[") && !normalized.contains("\"history\":[]");
    }

    // @author Ksawery
    private String retryManagerCustomerReport(Integer amount, String customerId) {
        String report = context.managerCustomerReport;
        if (reportContains(report, amount, customerId)) {
            return report;
        }
        int attempts = 6;
        long delayMs = 500;
        for (int i = 0; i < attempts; i++) {
            try {
                report = context.reportManagerService.managerCustomerReportRequest(customerId);
            } catch (Exception e) {
                report = null;
            }
            if (reportContains(report, amount, customerId)) {
                return report;
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return report;
    }

    // @author Peter
    private String retryManagerMerchantReport(Integer amount, String merchantId) {
        String report = context.managerMerchantReport;
        if (reportContains(report, amount, merchantId)) {
            return report;
        }
        int attempts = 6;
        long delayMs = 500;
        for (int i = 0; i < attempts; i++) {
            try {
                report = context.reportManagerService.managerMerchantReportRequest(merchantId);
            } catch (Exception e) {
                report = null;
            }
            if (reportContains(report, amount, merchantId)) {
                return report;
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return report;
    }

    // @author Nikolaj
    private boolean reportContains(String report, Integer amount, String merchantId) {
        return report != null
                && amount != null
                && merchantId != null
                && report.contains(amount.toString())
                && report.contains(merchantId);
    }
}
