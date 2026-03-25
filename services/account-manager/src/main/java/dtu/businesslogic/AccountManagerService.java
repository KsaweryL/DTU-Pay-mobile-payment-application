package dtu.businesslogic;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dtu.aggregate.AccountId;
import dtu.aggregate.ConcurrentIdState;
import dtu.aggregate.Customer;
import dtu.aggregate.Merchant;
import dtu.repositories.CustomerRepository;
import dtu.repositories.MerchantRepository;
import dtu.repositories.ReadModelRepository;
import messaging.Event;
import messaging.MessageQueue;

public class AccountManagerService {

	private MessageQueue queue;

	private CustomerRepository customerRepository;
	private MerchantRepository merchantRepository;
	private ReadModelRepository readRepository;
	private final ConcurrentHashMap<String, ConcurrentIdState> pendingIdValidations = new ConcurrentHashMap<>();

	public AccountManagerService(MessageQueue q) {
		queue = q;
		customerRepository = new CustomerRepository(q);
		merchantRepository = new MerchantRepository(q);
		readRepository = new ReadModelRepository(q);
		queue.addHandler("CustomerRegistrationRequested", this::policyCustomerRegistrationRequested);
		queue.addHandler("CustomerDeRegistrationRequested", this::commandCustomerDeregisteredRequest);
		queue.addHandler("MerchantRegistrationRequested", this::policyMerchantRegistrationRequested);
		queue.addHandler("MerchantDeRegistrationRequested", this::commandMerchantDeregisteredRequest);
		queue.addHandler("MerchantDeregisteredRequest", this::commandMerchantDeregisteredRequest);

		queue.addHandler("ValidateMidRequest", this::commandValidateMidRequest);
		queue.addHandler("ValidateCidRequest", this::commandValidateCidRequest);
	}

	// @author Tobias
	public void policyCustomerRegistrationRequested(Event event) {
		String correlationId = event.getArgument(0, String.class);
		String cpr = event.getArgument(1, String.class);
		String firstName = event.getArgument(2, String.class);
		String lastName = event.getArgument(3, String.class);
		String bankId = event.getArgument(4, String.class);

		AccountId existingAccountId = readRepository.getAccountIdFromCpr(cpr);
		if (existingAccountId != null) {
			queue.publish(new Event("CustomerRegistrationFailed", correlationId,
					"Customer is already registered with DTU Pay"));
			return;
		}

		commandRegisterCustomer(correlationId, firstName, lastName, cpr, bankId);
	}

	// @author Christoffer 
	public void policyMerchantRegistrationRequested(Event event) {
		String correlationId = event.getArgument(0, String.class);
		String cpr = event.getArgument(1, String.class);
		String firstName = event.getArgument(2, String.class);
		String lastName = event.getArgument(3, String.class);
		String bankId = event.getArgument(4, String.class);

		AccountId existingAccountId = readRepository.getMerchantAccountIdFromCpr(cpr);
		if (existingAccountId != null) {
			queue.publish(new Event("MerchantRegistrationFailed", correlationId,
					"Merchant is already registered with DTU Pay"));
			return;
		}

		commandRegisterMerchant(correlationId, firstName, lastName, cpr, bankId);
	}


	// @author Frederik 
	public void commandMerchantDeregisteredRequest(Event event) {
		String correlationId = event.getArgument(0, String.class);
		String accountId = event.getArgument(1, String.class);

		Merchant merchant = merchantRepository.getById(new AccountId(UUID.fromString(accountId)));
		merchant.delete(correlationId);
		merchantRepository.save(merchant);
	}

	// @author Ksawery 
	public void commandCustomerDeregisteredRequest(Event event) {
		String correlationId = event.getArgument(0, String.class);
		String accountId = event.getArgument(1, String.class);

		Customer customer = customerRepository.getById(new AccountId(UUID.fromString(accountId)));
		customer.delete(correlationId);
		customerRepository.save(customer);
	}

	// @author Fabian 
	private void commandValidateMidRequest(Event event) {
		String correlationId = event.getArgument(0, String.class);
		String merchantId = event.getArgument(1, String.class);

		// Merchant merchant = merchantRepository.getById(new
		// AccountId(UUID.fromString(merchantId)));
		// if (merchant == null) {
		// pendingIdValidations.remove(correlationId);
		// queue.publish(new Event("BankAccountDetailsFailed", correlationId, "Invalid
		// merchant id"));
		// return;
		// }

		updateValidationState(correlationId, state -> {
			state.merchantId = merchantId;
		});
	}

	// @author Nikolaj
	private void commandValidateCidRequest(Event event) {
		String correlationId = event.getArgument(0, String.class);
		String customerId = event.getArgument(1, String.class);

		// Customer customer = customerRepository.getById(new
		// AccountId(UUID.fromString(customerId)));
		// if (customer == null) {
		// pendingIdValidations.remove(correlationId);
		// queue.publish(new Event("BankAccountDetailsFailed", correlationId, "Invalid
		// customer id"));
		// return;
		// }

		updateValidationState(correlationId, state -> {
			state.customerId = customerId;
		});
	}

	// @author Nikolaj
	private void commandRegisterCustomer(String correlationId, String firstName, String lastName, String cpr,
			String bankId) {
		Customer customer = Customer.create(correlationId, firstName, lastName, cpr, bankId);
		customerRepository.save(customer);
	}

	// @author Tobias
	private void commandRegisterMerchant(String correlationId, String firstName, String lastName, String cpr,
			String bankId) {
		Merchant merchant = Merchant.create(correlationId, firstName, lastName, cpr, bankId);
		merchantRepository.save(merchant);
	}

	// private void commandProvideBankAccountDetails(String correlationId, String merchantBankAccount,
	// 		String customerBankAccount) {
	// 	queue.publish(new Event("BankAccountDetailsProvided", correlationId, merchantBankAccount, customerBankAccount));
	// }

	// @author Nikolaj
	private void updateValidationState(String correlationId, java.util.function.Consumer<ConcurrentIdState> updater) {
		ConcurrentIdState state = pendingIdValidations.computeIfAbsent(correlationId, id -> new ConcurrentIdState());
		synchronized (state) {
			updater.accept(state);
			if (state.isReady()) {
				pendingIdValidations.remove(correlationId);
				Customer customer = customerRepository.getById(new AccountId(UUID.fromString(state.customerId)));
				Merchant merchant = merchantRepository.getById(new AccountId(UUID.fromString(state.merchantId)));
				if (customer == null || merchant == null) {
					queue.publish(new Event("BankAccountDetailsFailed", correlationId, "Invalid customer or merchant"));
					queue.publish(new Event("ReleaseTokenRequested", correlationId));
					queue.publish(new Event("PaymentFailed", correlationId, "Invalid customer or merchant"));
					return;
				}
				queue.publish(new Event(
						"BankAccountDetailsProvided",
						correlationId,
						merchant.getBankAccount().getId(),
						customer.getBankAccount().getId()));
			}
		}
	}
}
