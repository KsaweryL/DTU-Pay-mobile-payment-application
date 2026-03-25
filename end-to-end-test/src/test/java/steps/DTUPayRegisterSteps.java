package steps;

import static org.junit.Assert.*;
import dtu.pay.resources.Customer;
import dtu.pay.resources.Merchant;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.Set;

import dtu.ws.fastmoney.User;

public class DTUPayRegisterSteps {
    private final Context context;

    public DTUPayRegisterSteps(Context context) {
        this.context = context;
    }

    //////////////////////////////////////////////////// GIVEN
    //////////////////////////////////////////////////// ////////////////////////////////////////////////////
    // @author Ksawery
    @Given("a customer with name {string}, last name {string}, and CPR {string} is registered with the bank")
    public void aCustomerWithNameLastNameAndCPRIsRegisteredWithTheBank(String name, String surname, String cprNumber) {
        name = context.resolveFirstName(name);
        surname = context.resolveLastName(surname);
        cprNumber = context.resolveCprNo(cprNumber);
        User user = new User();
        user.setFirstName(name);
        user.setLastName(surname);
        user.setCprNumber(cprNumber);

        String accountId;
        try {
            accountId = context.bank.createAccountWithBalance(
                    context.API_KEY,
                    user,
                    BigDecimal.valueOf(1000));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.bankAccountIds.add(accountId);
        context.customers.add(new Customer(cprNumber, name, surname, accountId));
        assertNotNull(accountId);
    }

    // @author Fabian
    @Given("a customer with name {string}, last name {string}, and CPR {string} is registered with the bank with an initial balance of {int} kr")
    public void aCustomerWithNameLastNameAndCPRIsRegisteredWithTheBankWithAnInitialBalance(String name, String surname,
            String cprNumber, int initialBalance) {
        name = context.resolveFirstName(name);
        surname = context.resolveLastName(surname);
        cprNumber = context.resolveCprNo(cprNumber);
        User user = new User();
        user.setFirstName(name);
        user.setLastName(surname);
        user.setCprNumber(cprNumber);

        String bankAccountId;
        try {
            bankAccountId = context.bank.createAccountWithBalance(
                    context.API_KEY,
                    user,
                    BigDecimal.valueOf(initialBalance));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.bankAccountIds.add(bankAccountId);
        context.customers.add(new Customer(cprNumber, name, surname, bankAccountId));
        assertNotNull(bankAccountId);

    }

    // a customer with name "firstName", last name "lastName", and CPR "cprNo" only registered in DTU Pay

    // @author Nikolaj
    @Given("the customer is registered with DTU pay using their bank account numbers")
    public void theCustomerIsRegisteredWithDTUPayUsingTheirBankAccountNumbers() {
        context.tempCustomer = context.customers.removeFirst();
        context.cid1 = context.accountManagerService.customerRegisterRequest(context.tempCustomer);
    }

    // @author Peter
    @Given("a customer with name {string}, last name {string}, CPR {string} and bankAccountId {string} is registered in DTU Pay using invalid bank account numbers")
    public void theCustomerIsRegisteredWithDTUPayUsingInvalidBankAccountNumbers(String name, String surname,
            String cprNumber, String invalidBankAccountId) {
        context.customers.add(new Customer(cprNumber, name, surname, invalidBankAccountId));
        context.tempCustomer = context.customers.removeFirst();
        context.cid1 = context.accountManagerService.customerRegisterRequest(context.tempCustomer);
    }

    // @author Tobias
    @Given("a merchant with name {string}, last name {string}, and CPR {string} is registered with the bank")
    public void aMerchantWithNameLastNameAndCPRIsRegisteredWithTheBank(String name, String surname, String cprNumber) {
        name = context.resolveFirstName(name);
        surname = context.resolveLastName(surname);
        cprNumber = context.resolveCprNo(cprNumber);
        User user = new User();
        user.setFirstName(name);
        user.setLastName(surname);
        user.setCprNumber(cprNumber);

        String accountId;
        try {
            accountId = context.bank.createAccountWithBalance(
                    context.API_KEY,
                    user,
                    BigDecimal.valueOf(1000));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.bankAccountIds.add(accountId);
        context.merchants.add(new Merchant(cprNumber, name, surname, accountId));
        assertNotNull(accountId);
    }
    // @author Christoffer
    @Given("a merchant with name {string}, last name {string}, and CPR {string} is registered with the bank with an initial balance of {int} kr")
    public void aMerchantWithNameLastNameAndCPRIsRegisteredWithTheBankWithAnInitialBalance(String name, String surname,
            String cprNumber, int initialBalance) {
        name = context.resolveFirstName(name);
        surname = context.resolveLastName(surname);
        cprNumber = context.resolveCprNo(cprNumber);
        User user = new User();
        user.setFirstName(name);
        user.setLastName(surname);
        user.setCprNumber(cprNumber);

        String bankAccountId;
        try {
            bankAccountId = context.bank.createAccountWithBalance(
                    context.API_KEY,
                    user,
                    BigDecimal.valueOf(initialBalance));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.bankAccountIds.add(bankAccountId);
        context.merchants.add(new Merchant(cprNumber, name, surname, bankAccountId));
        assertNotNull(bankAccountId);

    }
    // @author Frederik
    @Given("the merchant is registered with DTU pay using their bank account numbers")
    public void theMerchantIsRegisteredWithDTUPayUsingTheirBankAccountNumbers() {
        context.tempMerchant = context.merchants.removeFirst();
        context.mid1 = context.accountManagerService.merchantRegisterRequest(context.tempMerchant);
    }

    
    //////////////////////////////////////////////////// WHEN
    //////////////////////////////////////////////////// /////////////////////////////////////////////////////
    // @author Ksawery
    @When("the merchant deregister from DTU pay")
    public void theMerchantDeregisterFromDTUPay() {
        context.result = context.accountManagerService.merchantDeregisterRequest(context.mid1);
    }

    // @author Christoffer
    @When("the customer deregister from DTU pay")
    public void theCustomerDeregisterFromDTUPay() {
        context.result = context.accountManagerService.customerDeregisterRequest(context.cid1);
    }

    // @author Nikolaj
    @When("the customer register with DTU pay using their bank account numbers")
    public void theCustomerRegisterWithDTUPayUsingTheirBankAccountNumbers() {
        context.tempCustomer = context.customers.removeFirst();
        context.cid1 = context.accountManagerService.customerRegisterRequest(context.tempCustomer);
    }

    // @author Tobias
    @When("the merchant register with DTU pay using their bank account numbers")
    public void theMerchantRegisterWithDTUPayUsingTheirBankAccountNumbers() {
        context.tempMerchant = context.merchants.removeFirst();
        context.mid1 = context.accountManagerService.merchantRegisterRequest(context.tempMerchant);
    }

    // @author Peter
    @When("both customers register with DTU pay using their bank account numbers at the same time")
    public void bothCustomersRegisterWithDTUPayUsingTheirBankAccountNumbersAtTheSameTime() {

        var thread1 = new Thread(() -> {
            context.resultCustomerRegistration1
                    .complete(context.accountManagerService.customerRegisterRequest(context.customers.removeFirst()));
        });
        var thread2 = new Thread(() -> {
            context.resultCustomerRegistration2
                    .complete(context.accountManagerService.customerRegisterRequest(context.customers.removeFirst()));
        });
        thread1.start();
        thread2.start();

    }

    // @author Ksawery
    @When("both merchants register with DTU pay using their bank account numbers at the same time")
    public void bothMerchantsRegisterWithDTUPayUsingTheirBankAccountNumbersAtTheSameTime() {
        var thread1 = new Thread(() -> {
            context.resultMerchantRegistration1
                    .complete(context.accountManagerService.merchantRegisterRequest(context.merchants.removeFirst()));
        });
        var thread2 = new Thread(() -> {
            context.resultMerchantRegistration2
                    .complete(context.accountManagerService.merchantRegisterRequest(context.merchants.removeFirst()));
        });
        thread1.start();
        thread2.start();
    }
    
    // @author Frederik
    @When("the customer register again with DTU pay using their bank account numbers")
    public void theCustomerRegisterAgainWithDTUPayUsingTheirBankAccountNumbers() {
        Customer sameCustomer = context.tempCustomer;
        try {
            context.result = context.accountManagerService.customerRegisterRequest(sameCustomer);
        } catch (Exception e) {
            context.lastException1 = e;
        }
    }

    // @author Christoffer
    @When("the merchant register again with DTU pay using their bank account numbers")
    public void theMerchantRegisterAgainWithDTUPayUsingTheirBankAccountNumbers() {
        Merchant sameMerchant = context.tempMerchant;
        try {
            context.result = context.accountManagerService.merchantRegisterRequest(sameMerchant);
        } catch (Exception e) {
            context.lastException1 = e;
        }
    }

    // @author Peter
    @Then("the merchant receives a 4xx response with an error message saying {string}")
    public void theMerchantReceivesA4xxResponseWithAnErrorMessageSaying(String errorMessage) {
        
        assertNotNull("Expected an error but request succeeded", context.lastException1);
        assertTrue(context.lastException1 instanceof WebApplicationException);

        WebApplicationException ex = (WebApplicationException) context.lastException1;
        Response exceptionResponse = ex.getResponse();
        String body = exceptionResponse.readEntity(String.class);
        int status = exceptionResponse.getStatus();

        Set<Integer> validStatuses = Set.of(400, 409);
        assertTrue(validStatuses.contains(status));
        assertTrue(body.contains(errorMessage));
        
    }

    //////////////////////////////////////////////////// THEN
    //////////////////////////////////////////////////// ////////////////////////////////////////////////////

    // @author Peter
    @Then("the customer is no longer registered with DTU pay")
    public void theCustomerIsNoLongerRegisteredWithDTUPay() {
        assertEquals("Customer deregistered", context.result);
    }

    // @author Tobias
    @Then("the merchant is no longer registered with DTU pay")
    public void theMerchantIsNoLongerRegisteredWithDTUPay() {
        assertEquals("Merchant deregistered", context.result);
    }

    // @author Frederik
    @Then("the customer is registered with DTU pay")
    public void theCustomerIsRegisteredWithDTUPay() {
        assertNotNull(context.cid1);
    }

    // @author Christoffer
    @Then("the merchant is registered with DTU pay")
    public void theMerchantIsRegisteredWithDTUPay() {
        assertNotNull(context.mid1);
    }

    // @author Ksawery
    @Then("both customers recieve the correct unique ids and are registered with DTU pay")
    public void bothCustomersRecieveTheCorrectUniqueIdsAndAreRegisteredWithDTUPay() {
        String resultingIdCustomer1 = context.resultCustomerRegistration1.join();
        String resultingIdCustomer2 = context.resultCustomerRegistration2.join();

        assertNotNull(resultingIdCustomer1);
        assertNotNull(resultingIdCustomer2);
        assertNotEquals(resultingIdCustomer1, resultingIdCustomer2);
    }

    // @author Fabian
    @Then("both merchants recieve the correct unique ids and are registered with DTU pay")
    public void bothMerchantsRecieveTheCorrectUniqueIdsAndAreRegisteredWithDTUPay() {
        String resultingIdMerchant1 = context.resultMerchantRegistration1.join();
        String resultingIdMerchant2 = context.resultMerchantRegistration2.join();

        assertNotNull(resultingIdMerchant1);
        assertNotNull(resultingIdMerchant2);
        assertNotEquals(resultingIdMerchant1, resultingIdMerchant2);
    }

}
