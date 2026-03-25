 Feature: Token Request
 
    # @author Nikolaj
    Scenario: Customer requests token
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer register with DTU pay using their bank account numbers
        When the customer requests 4 tokens
        Then the customer receives tokens

    # @author Nikolaj
    Scenario: Customer requests more tokens than allowed
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer register with DTU pay using their bank account numbers
        When the customer requests 10 tokens
        Then the customer receives a 4xx response with an error message saying "Cannot request more than 5 tokens at a time"

    # @author Ksawery
    Scenario: Customer request tokens when not allowed
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer register with DTU pay using their bank account numbers
        And the customer has 3 tokens
        When the customer requests 3 tokens
        Then the customer receives a 4xx response with an error message saying "Cannot request more tokens until existing tokens are used"
    
    # @author Fabian
    Scenario: Multiple customers request tokens at the same time
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And a customer with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And both customers register with DTU pay using their bank account numbers at the same time
        When both customers request 5 tokens
        Then both customers receive unique tokens each

    # @author Fabian
    Scenario: Token remains unused after payment failed
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank with an initial balance of 20 kr
        And the customer register with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant register with DTU pay using their bank account numbers
        And the customer has 3 tokens
        When the merchant initiates a DTU Pay payment for 50 kr with the customers token
        Then the payment fails with an error message "Payment failed: Debtor balance will be negative"
        And the merchant initiates another DTU Pay payment for 10 kr with the customers token
        And the payment is successful