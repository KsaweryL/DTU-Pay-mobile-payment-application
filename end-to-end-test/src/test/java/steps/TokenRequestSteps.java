package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class TokenRequestSteps {

    private final Context context;

    public TokenRequestSteps(Context context) {
        this.context = context;
    }

    // ============ GIVEN ============

    // @author Fabian
    @Given("the customer has {int} tokens")
    public void theCustomerHasTokens(int tokenNumber) {
        try {
            context.generatedTokens1 = context.tokenService.generateTokensForCustomer(
                    context.cid1,
                    tokenNumber);

            String firstToken = context.generatedTokens1
                .replace("[", "")
                .replace("]", "")
                .split(",")[0]
                .trim()
                .replace("\"", "");


            context.tokenList.put(context.cid1, firstToken);
        } catch (Exception e) {
            context.lastException1 = e;
        }
    }

    // ============ WHEN ============

    // @author Fabian
    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int tokenNumber) {
        try {
            context.generatedTokens1 = context.tokenService.generateTokensForCustomer(
                    context.cid1,
                    tokenNumber);
        } catch (Exception e) {
            context.lastException1 = e;
        }
    }

    // @author Ksawery
    @When("both customers request {int} tokens")
    public void bothCustomersRequestTokens(int tokenNumber) {
        context.cid1 = context.resultCustomerRegistration1.join();
        context.cid2 = context.resultCustomerRegistration2.join();

        assertNotNull(context.cid1);
        assertNotNull(context.cid2);
        assertNotEquals(context.cid1, context.cid2);

        try {
            context.generatedTokens1 = context.tokenService.generateTokensForCustomer(
                    context.cid1,
                    tokenNumber);
        } catch (Exception e) {
            context.lastException1 = e;
        }

        try {
            context.generatedTokens2 = context.tokenService.generateTokensForCustomer(
                    context.cid2,
                    tokenNumber);
        } catch (Exception e) {
            context.lastException2 = e;
        }
    }

    
    // @author Peter
    @When("the payment fails with a 4xx response and an error message {string}")
    public void thePaymentFailsWit4xxResponseAndErrorMessage(String error) {

        assertNotNull("Expected an error but request succeeded", context.lastException1);
        assertTrue(context.lastException1 instanceof WebApplicationException);

        WebApplicationException ex = (WebApplicationException) context.lastException1;
        Response exceptionResponse = ex.getResponse();
        String body = exceptionResponse.readEntity(String.class);
        int status = exceptionResponse.getStatus();

        // System.out.println("HTTP " + status + " BODY: " + body + " for string
        // "+error);

        assertTrue(status == 400 || status == 409);

        assertTrue(body.contains(error));
    }

    // ============ THEN ============

    // @author Tobias
    @Then("the customer receives tokens")
    public void theCustomerReceivesValidTokens() {
        assertNull("Expected success but got exception: " + context.lastException1, context.lastException1);

        assertNotNull(context.generatedTokens1);
    }

    // @author Tobias
    @Then("the customer receives a 4xx response with an error message saying {string}")
    public void theCustomerReceivesAnErrorMessageSaying(String error) {

        assertNotNull("Expected an error but request succeeded", context.lastException1);
        assertTrue(context.lastException1 instanceof WebApplicationException);

        WebApplicationException ex = (WebApplicationException) context.lastException1;
        Response exceptionResponse = ex.getResponse();
        String body = exceptionResponse.readEntity(String.class);
        int status = exceptionResponse.getStatus();

        Set<Integer> validStatuses = Set.of(400, 409);
        assertTrue(validStatuses.contains(status));
        assertTrue(body.contains(error));
    }
    
    // @author Christoffer
    @Then("both customers receive unique tokens each")
    public void bothCustomersReceiveUniqueValidTokensEach() {
        assertNull("Expected success but got exception: " + context.lastException1, context.lastException1);
        assertNull("Expected success but got exception: " + context.lastException2, context.lastException2);

        assertNotNull(context.generatedTokens1);

        assertNotNull(context.generatedTokens2);

        assertNotEquals(context.generatedTokens1, context.generatedTokens2);
    }

    // @author Frederik
    @Then("the customer's token remains valid and unused")
    public void theCustomersTokenRemainsValidAndUnused() {
        String obtainedTokens = context.tokenService.getTokens(context.cid1);
        String firstObtainedToken = obtainedTokens
                .replace("[", "")
                .replace("]", "")
                .split(",")[0]
                .trim();

        assertEquals(context.unusedToken, firstObtainedToken);
    }

}
