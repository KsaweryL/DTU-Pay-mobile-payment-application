Feature: Payment Processing in DTU Pay System

    # @author Christoffer
    Scenario: Merchant initiates payment
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the customer has 5 tokens
        And the merchant is registered with DTU pay using their bank account numbers
        When the merchant initiates a DTU Pay payment for 50 kr with the customers token
        Then the payment is successful

    # @author Christoffer
    Scenario: Customer used invalid bank information
        Given a customer with name "firstName", last name "lastName", CPR "cprNo" and bankAccountId "invalid-bank-account-id" is registered in DTU Pay using invalid bank account numbers
        And the customer has 5 tokens
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        When the merchant initiates a DTU Pay payment for 50 kr with the customers token
        Then the payment fails with an error message "Debtor account does not exist"

    # @author Tobias
    Scenario: Customer token can not be used after deregistering
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        When the customer deregister from DTU pay
        And the merchant initiates a DTU Pay payment for 50 kr with the customers token
        Then the payment fails with an error message "Invalid token used for payment"

    # @author Tobias
    Scenario: Invalid token used for payment
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        When the merchant initiates a DTU Pay payment for 50 kr with an invalid token
        Then the payment fails with an error message "Token is invalid or already used"

    # @author Peter
    Scenario: Already used token is used for payment
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        When the merchant initiates a DTU Pay payment for 50 kr with one of the customer's tokens
        And the merchant initiates another DTU Pay payment for 30 kr with the same token
        Then the payment fails with an error message "Token is invalid or already used"

    # @author Peter
    Scenario: Merchant initiates payment with customer with insufficient funds
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank with an initial balance of 20 kr
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        When the merchant initiates a DTU Pay payment for 50 kr with the customers token
        Then the payment fails with an error message "Debtor balance will be negative"

    # @author Peter
    Scenario: Negative payment amount
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        When the merchant initiates a DTU Pay payment for -50 kr with the customers token
        Then the payment fails with an error message "Amount needs to be positive"

    # @author Peter
    Scenario: Merchant recieves the money
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank with an initial balance of 1000 kr
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank with an initial balance of 500 kr
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        When the merchant initiates a DTU Pay payment for 50 kr with the customers token
        Then the payment is successful
        And the balance of the merchant at the bank is 550 kr

    # @author Peter
    Scenario: Customer uses money
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank with an initial balance of 1000 kr
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank with an initial balance of 500 kr
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        When the merchant initiates a DTU Pay payment for 50 kr with the customers token
        Then the payment is successful
        And the balance of the customer at the bank is 950 kr

 # ## refund scenario?
