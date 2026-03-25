package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

// @author Christoffer
public class Hooks {

    private final Context context;

    public Hooks(Context context) {
        this.context = context;
    }

    @Before
    public void beforeScenario() {
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) { 
            String redStart = "\u001B[31m";
            String redEnd = "\u001B[0m";
            System.err.println(redStart);
            System.err.println("Scenario failed: " + scenario.getName());
            System.err.println(context);
            System.err.println(redEnd);
        }
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
        context.reportManagerService.close();
    }
}
