package steps;

import static org.junit.Assert.*;
import java.util.concurrent.CompletableFuture;
// import dtu.pay.resources.Customer;
import dtu.pay.resources.PaymentRequest;
// import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.WebApplicationException;

public class PaymentSteps {

    private final Context context;

    public PaymentSteps(Context context) {
        this.context = context;
    }

    // ============ GIVEN ============

    // ============ WHEN ============
    // @author Fabian
    @When("the merchant initiates a DTU Pay payment for {int} kr with one of the customer's tokens")
    public void theMerchantInitiatesADTUPayPaymentForKrWithOneOfTheCustomerSTokens(int int1) {
        context.unusedToken = context.tokenList.get(context.cid1);
        context.lastException1 = null;
        context.result = context.paymentService.initiatePayment(
                new PaymentRequest(int1, context.mid1, context.unusedToken));
    }

    //@author Nikolaj
    @When("the merchant initiates another DTU Pay payment for {int} kr with the same token")
    public void theMerchantInitiatesAnotherDTUPayPaymentForKrWithTheSameToken(Integer int1) {
        context.lastException1 = null;
        context.result = context.paymentService.initiatePayment(
                new PaymentRequest(int1, context.mid1, context.unusedToken));
    }

    // @author Peter
    @Then("the second payment fails with error message {string}")
    public void theSecondPaymentFailsWithErrorMessage(String string) {
        assertPaymentFailure(string);
    }

    // @author Tobias
    @When("the merchant initiates a DTU Pay payment for {int} kr with the customers token")
    public void theMerchantInitiatesADTUPayPaymentForKrWithTheCustomersToken(Integer int1) {
        context.unusedToken = context.tokenList.get(context.cid1);
        try {
            PaymentRequest paymentRequest = new PaymentRequest(int1, context.mid1, context.unusedToken);
            context.result = context.paymentService.initiatePayment(paymentRequest);
        } catch (Exception e) {
            context.lastException1 = e;
        }
    }

    // @author Christoffer
    @When("the merchant initiates a two DTU Pay payments for {int} kr with the same token")
    public void theMerchantInitiatesATwoDTUPayPaymentsForKrWithTheSameToken(Integer int1) {
        context.unusedToken = context.tokenList.get(context.cid1);
        try {
            PaymentRequest paymentRequest1 = new PaymentRequest(int1, context.mid1, context.unusedToken);
            PaymentRequest paymentRequest2 = new PaymentRequest(int1, context.mid1, context.unusedToken);

            CompletableFuture<String> f1 = CompletableFuture
                    .supplyAsync(() -> context.paymentService.initiatePayment(paymentRequest1));
            CompletableFuture<String> f2 = CompletableFuture
                    .supplyAsync(() -> context.paymentService.initiatePayment(paymentRequest2));

            context.result = f1.handle((v, ex) -> ex == null ? v : ex.getMessage()).join();
            context.result2 = f2.handle((v, ex) -> ex == null ? v : ex.getMessage()).join();

        } catch (Exception e) {
            context.lastException1 = e;
        }
    }

    // @author Frederik
    @When("the merchant initiates a DTU Pay payment for {int} kr with an invalid token")
    public void theMerchantInitiatesADTUPayPaymentForKrWithAnInvalidToken(Integer int1) {
        context.unusedToken = "invalid-token";
        context.lastException1 = null;
        context.result = context.paymentService.initiatePayment(
                new PaymentRequest(int1, context.mid1, context.unusedToken));
    }

    // ============ THEN ============

    // @author Ksawery
    @Then("the payment fails with an error message {string}")
    public void thePaymentFailsWithErrorMessage(String string) {
        assertPaymentFailure(string);
    }

    // @author Nikolaj
    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertEquals("Payment completed successfully", context.result);
    }

    // @author Fabian
    @Then("one payment is successful and the other one fails with error message {string}")
    public void onePaymentIsSuccessfulAndTheOtherOneFailsWithErrorMessage(String string) {
        String successMessage = "Payment completed successfully";
        boolean hasError = matchesExpected(string, context.result) || matchesExpected(string, context.result2);
        boolean hasSuccess = successMessage.equals(context.result) || successMessage.equals(context.result2);
        assertTrue(hasError && hasSuccess);
    }

    // @author Peter
    private boolean matchesExpected(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return expected.equals(actual) || actual.contains(expected);
    }

    // @author Frederik
    private void assertPaymentFailure(String expected) {
        String actual = context.result;
        if (actual == null && context.lastException1 instanceof WebApplicationException ex) {
            actual = ex.getResponse().readEntity(String.class);
        }

        if (actual != null && !expected.startsWith("Payment failed:")
                && actual.startsWith("Payment failed:")) {
            actual = actual.replaceFirst("^Payment failed:\\s*", "");
        }

        assertEquals(expected, actual);
    }
    
    // @author Ksawery
    @Then("the merchant initiates another DTU Pay payment for {int} kr with the customers token")
    public void theMerchantInitiatesAnotherDTUPayPaymentForKrWithTheCustomersToken(Integer int1) {
        context.unusedToken = context.tokenList.get(context.cid1);
        try {
            PaymentRequest paymentRequest = new PaymentRequest(int1, context.mid1, context.unusedToken);
            context.result = context.paymentService.initiatePayment(paymentRequest);
        } catch (Exception e) {
            context.lastException1 = e;
        }
    }
}
