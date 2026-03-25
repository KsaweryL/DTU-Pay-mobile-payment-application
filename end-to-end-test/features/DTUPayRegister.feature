Feature: Bank Registration/Deregistration in DTU Pay System

    # @author Christoffer
    Scenario: Register merchant
        Given a merchant with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        When the merchant register with DTU pay using their bank account numbers
        Then the merchant is registered with DTU pay

    # @author Christoffer
    Scenario: Register customer
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        When the customer register with DTU pay using their bank account numbers
        Then the customer is registered with DTU pay

    # @author Christoffer
    Scenario: Two customers at the same time
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And a customer with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        When both customers register with DTU pay using their bank account numbers at the same time
        Then both customers recieve the correct unique ids and are registered with DTU pay

    # @author Frederik
    Scenario: Two merchants at the same time
        Given a merchant with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And a merchant with name "otherFirstName", last name "otherLastName", and CPR "otherCprNo" is registered with the bank
        When both merchants register with DTU pay using their bank account numbers at the same time
        Then both merchants recieve the correct unique ids and are registered with DTU pay

    # @author Ksawery
    Scenario: Same customer registers twice
        Given a customer with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        When the customer register again with DTU pay using their bank account numbers
        Then the customer receives a 4xx response with an error message saying "Customer is already registered with DTU Pay"

    # @author Ksawery
    Scenario: Same merchant registers twice
        Given a merchant with name "firstName", last name "lastName", and CPR "cprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        When the merchant register again with DTU pay using their bank account numbers
        Then the merchant receives a 4xx response with an error message saying "Merchant is already registered with DTU Pay"

    # @author Ksawery
    Scenario: Customer deregister
        Given a customer with name "firstName", last name "Johnson", and CPR "cprNo" is registered with the bank
        And the customer is registered with DTU pay using their bank account numbers
        When the customer deregister from DTU pay
        Then the customer is no longer registered with DTU pay
        # TODO: Add check for making sure data is deleted from readmodel

    # @author Ksawery
    Scenario: Merchant deregister
        Given a merchant with name "firstName", last name "Smith", and CPR "cprNo" is registered with the bank
        And the merchant is registered with DTU pay using their bank account numbers
        When the merchant deregister from DTU pay
        Then the merchant is no longer registered with DTU pay
        # TODO: Add check for making sure data is deleted from readmodel




