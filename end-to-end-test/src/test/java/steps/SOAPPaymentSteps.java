package steps;

import static org.junit.Assert.assertEquals;

import dtu.ws.fastmoney.BankServiceException_Exception;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;



public class SOAPPaymentSteps {

    Context context;

    public SOAPPaymentSteps(Context context) {
        this.context = context;
    }

    @Before
    public void setup(){
    }

    @After
    public void tearDown() {
        for (String accountId : context.bankAccountIds) {
            try {
                context.bank.retireAccount(context.API_KEY, accountId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        context.bankAccountIds.clear();
        context.accountManagerService.close();
        context.paymentService.close();
        context.tokenService.close();
    }

    // ============ THEN ============

    // @author Nikolaj
    @Then("the balance of the customer at the bank is {int} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(Integer balance) {
        try {
            assertEquals(balance.intValue(), context.bank.getAccount(context.tempCustomer.customerBankId()).getBalance().intValue());
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
    }

    // @author Nikolaj
    @Then("the balance of the merchant at the bank is {int} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(Integer balance) {
        try {
            assertEquals(balance.intValue(), context.bank.getAccount(context.tempMerchant.merchantBankId()).getBalance().intValue());
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
    }

}
