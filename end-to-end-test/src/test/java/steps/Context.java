package steps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import dtu.pay.AccountManagerService;
import dtu.pay.PaymentService;
import dtu.pay.ReportManagerService;
import dtu.pay.TokenService;
import dtu.pay.resources.Customer;
import dtu.pay.resources.Merchant;
import dtu.pay.resources.Payment;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;

// @author Frederik, Fabian, Nikolaj, Peter, Tobias, Christoffer, Ksawery
public class Context {;
    AccountManagerService accountManagerService = new AccountManagerService();
    PaymentService paymentService = new PaymentService();
    ArrayList<Customer> customers = new ArrayList<>();
    Customer tempCustomer;
    ArrayList<Merchant> merchants = new ArrayList<>();
    Merchant tempMerchant;

    String mid1;
    String mid2;
    String cid1;
    String cid2;

    ConcurrentHashMap<String, String> tokenList = new ConcurrentHashMap<>();
    DTUPayRegisterSteps dtuPayRegisterSteps;

    String result;
    String result2;

    TokenService tokenService = new TokenService();
    String customerId;
    String generatedTokens1;
    String generatedTokens2;
    Exception lastException1;
    Exception lastException2;
    Payment payment1;
    String firstToken;
    String unusedToken;
    String paymentTokenUsed;

    String API_KEY = "not_real_key";
    BankService bank = new BankService_Service().getBankServicePort();
    List<String> bankAccountIds = new ArrayList<>();

    CompletableFuture<String> resultCustomerRegistration1 = new CompletableFuture<String>();
    CompletableFuture<String> resultCustomerRegistration2 = new CompletableFuture<String>();

    CompletableFuture<String> resultMerchantRegistration1 = new CompletableFuture<String>();
    CompletableFuture<String> resultMerchantRegistration2 = new CompletableFuture<String>();

    String firstName;
    String lastName;
    String cprNo;
    String otherFirstName;
    String otherLastName;
    String otherCprNo;

    ReportManagerService reportManagerService = new ReportManagerService();
    String customerReport;
    String merchantReport;
    String managerReport;
    String customerReportHistory;
    String merchantReportHistory;
    String managerReportHistory;
    String managerMerchantReport;
    String managerCustomerReport;
    String managerMerchantReportHistory;
    String managerCustomerReportHistory;
    String fakeBankId;
    

    String resolveFirstName(String value) {
        if ("firstName".equals(value)) {
            if (firstName == null) {
                firstName = randomAlpha("First", 6);
            }
            return firstName;
        }
        if ("otherFirstName".equals(value)) {
            if (otherFirstName == null) {
                otherFirstName = randomAlpha("First", 6);
            }
            return otherFirstName;
        }
        return value;
    }

    String resolveLastName(String value) {
        if ("lastName".equals(value)) {
            if (lastName == null) {
                lastName = randomAlpha("Last", 6);
            }
            return lastName;
        }
        if ("otherLastName".equals(value)) {
            if (otherLastName == null) {
                otherLastName = randomAlpha("Last", 6);
            }
            return otherLastName;
        }
        return value;
    }

    String resolveCprNo(String value) {
        if ("cprNo".equals(value)) {
            if (cprNo == null) {
                cprNo = randomCpr();
            }
            return cprNo;
        }
        if ("otherCprNo".equals(value)) {
            if (otherCprNo == null) {
                otherCprNo = randomCpr();
            }
            return otherCprNo;
        }
        return value;
    }

    private static String randomAlpha(String prefix, int length) {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * letters.length());
            sb.append(letters.charAt(idx));
        }
        return sb.toString();
    }

    private static String randomCpr() {
        int first = (int) (Math.random() * 1_000_000);
        int second = (int) (Math.random() * 10_000);
        return String.format("%06d-%04d", first, second);
    }

    @Override
    public String toString() {
        return String.format(
            "Context {\n" +
            "  ids: { cid1: %s, cid2: %s, mid1: %s, mid2: %s }\n" +
            "  results: { result: %s, result2: %s, lastException1: %s, lastException2: %s }\n" +
            "  exceptions: { lastException1: %s, lastException2: %s }\n" +
            "  tokens: { generatedTokens1: %s, generatedTokens2: %s, unusedToken: %s, paymentTokenUsed: %s }\n" +
            "  reports: { customerReport: %s, merchantReport: %s, managerReport: %s, customerReportHistory: %s, merchantReportHistory: %s, managerReportHistory: %s, managerMerchantReport: %s, managerMerchantReportHistory: %s }\n" +
            "  bankAccountIds: %s\n" +
            "}",
            cid1, cid2, mid1, mid2,
            result, result2, formatException(lastException1), formatException(lastException2),
            formatException(lastException1), formatException(lastException2),
            generatedTokens1, generatedTokens2, unusedToken, paymentTokenUsed,
            abbrev(customerReport), abbrev(merchantReport), abbrev(managerReport),
            abbrev(customerReportHistory), abbrev(merchantReportHistory), abbrev(managerReportHistory),
            abbrev(managerMerchantReport), abbrev(managerMerchantReportHistory),
            bankAccountIds
        );
    }

    private static String formatException(Exception ex) {
        if (ex == null) {
            return "null";
        }
        String message = ex.getMessage();
        return ex.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }

    private static String abbrev(String value) {
        if (value == null) {
            return "null";
        }
        int limit = 120;
        return value.length() <= limit ? value : value.substring(0, limit) + "...";
    }
}
