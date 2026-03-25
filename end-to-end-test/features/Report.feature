Feature: Report Generation in DTU Pay System

    ## Customer scenarios

    # @author Fabian
    Scenario: Customer ask for an empty report
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        When the customer requests a report
        Then the customer receives an empty report showing no payments made

    # @author Fabian
    Scenario: Customer ask for report
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the customer requests a report
        Then the customer receives a report showing the payment of 100 kr to the merchant

    # @author Fabian
    Scenario: Customer ask for report history
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the customer requests a report
        And the customer requests a report history
        Then the customer receives a report history with at least one entry

    ## Merchant scenarios

    # @author Fabian
    Scenario: Merchant ask for report
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the merchant requests a report
        Then the merchant receives a report showing the payment of 100 kr from the customer token

    # @author Nikolaj
    Scenario: Merchant ask for report history
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the merchant requests a report
        And the merchant requests a report history
        Then the merchant receives a report history with at least one entry


    ## Manager Scenarios

    # @author Frederik
    Scenario: Manager ask for full report
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the manager requests a report
        Then the manager receives a report showing all payments made through DTU pay

    # @author Frederik
    Scenario: Manager ask for report for a merchant
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the manager requests a report for the merchant
        Then the manager receives a report showing the payment of 100 kr to the merchant

    # @author Nikolaj
    Scenario: Manager ask for report for a customer
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the manager requests a report for the customer
        Then the manager receives a report showing the payment of 100 kr from the customer 

    # @author Nikolaj
    Scenario: Manager ask for report history
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the manager requests a report
        And the manager requests a report history
        Then the manager receives a report history with at least one entry

    # @author Tobias 
    Scenario: Manager ask for report history for a merchant
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the merchant requests a report
        And the manager requests a report history for the merchant
        Then the manager receives a report history for the merchant

    # @author Tobias
    Scenario: Manager ask for report history for a customer
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        And the customer has 5 tokens
        And the customer successfully makes a payment of 100 kr to the merchant
        When the customer requests a report
        And the manager requests a report history for the customer
        Then the manager receives a report history for the customer