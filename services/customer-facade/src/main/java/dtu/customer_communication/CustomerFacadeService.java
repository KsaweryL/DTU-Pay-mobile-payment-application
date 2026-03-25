package dtu.customer_communication;

import dtu.customer_communication.Exceptions.HttpException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Singleton;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

@Singleton
public class CustomerFacadeService {

	private static final long RESPONSE_TIMEOUT_SECONDS = 10;
	private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequest = new ConcurrentHashMap<>();

	private final MessageQueue queue;

	public CustomerFacadeService() {
		this(new RabbitMqQueue());
	}

	public CustomerFacadeService(MessageQueue q) {
		this.queue = q;
		q.addHandler("CustomerCreated", this::commandSendCreationSuccessfulResponse);
		q.addHandler("CustomerDeregistered", this::commandCustomerDeregistered);
		q.addHandler("PaymentTokensGenerated", this::commandSendTokensGeneratedResponse);
		q.addHandler("PaymentTokensRejected", this::commandSendTokensRejectedResponse);
		q.addHandler("CustomerFullReportReceived", this::commandSendCustomerFullReportResponse);
		q.addHandler("CustomerRegistrationFailed", this::commandSendCreationFailedResponse);
		q.addHandler("CustomerReportHistoryReceived", this::commandSendCustomerReportHistoryResponse);
		q.addHandler("ReportGenerationFailed", this::commandSendReportGenerationFailedResponse);
	}

	private void commandSendReportGenerationFailedResponse(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			String reason = e.getArgument(1, String.class);
			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.completeExceptionally(new HttpException(reason));
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Nikolaj
	private void commandSendCreationSuccessfulResponse(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			UUID newCustomerId = AccountId.fromEvent(e);
			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.complete(newCustomerId.toString());
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Nikolaj
	private void commandSendCreationFailedResponse(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			String reason = e.getArgument(1, String.class);
			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.completeExceptionally(new HttpException(reason));
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Tobias
	private void commandCustomerDeregistered(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.complete("Customer deregistered");
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Christoffer
	private void commandSendTokensGeneratedResponse(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			// String customerId = e.getArgument(1, String.class);
			String[] tokens = e.getArgument(2, String[].class);

			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.complete(String.join(",", tokens));
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Christoffer
	private void commandSendTokensRejectedResponse(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			// String customerId = e.getArgument(1, String.class);
			String reason = e.getArgument(2, String.class);

			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.completeExceptionally(new HttpException(reason));
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Tobias
	public String registerCustomer(Customer customer) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequest.put(correlationId, response);

		queue.publish(new Event("CustomerRegistrationRequested",
				correlationId,
				customer.firstName(),
				customer.lastName(),
				customer.cpr(),
				customer.customerBankId()));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

		} catch (TimeoutException e) {
			pendingRequest.remove(correlationId);
			throw new RuntimeException("Timed out waiting for CustomerRegistered", e);

		} catch (InterruptedException e) {
			pendingRequest.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);

		} catch (ExecutionException e) {
			pendingRequest.remove(correlationId);
			Throwable cause = e.getCause();
			if (cause instanceof HttpException httpException) {
				throw httpException;
			}
			throw new RuntimeException(cause);
		}
	}

	// @author Frederik
	public String deregisterCustomer(String cid) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequest.put(correlationId, response);
		queue.publish(new Event("CustomerDeRegistrationRequested", correlationId, cid));
		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequest.remove(correlationId);
			throw new RuntimeException("Timed out waiting for CustomerDeregistered", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequest.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Nikolaj
	public String getTokens(String customerId, int numberOfTokens) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequest.put(correlationId, response);

		queue.publish(new Event("RequestPaymentTokens", correlationId, customerId, numberOfTokens));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequest.remove(correlationId);
			throw new RuntimeException("Timed out waiting for RequestPaymentTokens", e);
		} catch (InterruptedException e) {
			pendingRequest.remove(correlationId);
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);

		} catch (ExecutionException e) {
			pendingRequest.remove(correlationId);

			Throwable cause = e.getCause();
			if (cause instanceof HttpException tokenRequestException) {
				throw tokenRequestException;
			}

			throw new RuntimeException(cause);
		}
	}

	// @author Peter
	public String getCustomerReport(String customerId) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequest.put(correlationId, response);
		queue.publish(new Event("GetCustomerReportRequested", correlationId, customerId));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequest.remove(correlationId);
			throw new RuntimeException("Timed out waiting for customerGetReportResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequest.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Peter
	private void commandSendCustomerFullReportResponse(Event event) {
		try {
			String correlationId = event.getArgument(0, String.class);
			String report = event.getArgument(1, String.class);
			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.complete(report);
			}
		} catch (RuntimeException ex) {
		}
	}

	// @author Peter
	public String getCustomerReportHistory(String customerId) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequest.put(correlationId, response);
		queue.publish(new Event("GetCustomerReportHistoryRequested", correlationId, customerId));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequest.remove(correlationId);
			throw new RuntimeException("Timed out waiting for customerGetReportHistoryResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequest.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Frederik
	private void commandSendCustomerReportHistoryResponse(Event event) {
		try {
			String correlationId = event.getArgument(0, String.class);
			String reportHistory = event.getArgument(1, String.class);
			CompletableFuture<String> response = pendingRequest.remove(correlationId);
			if (response != null) {
				response.complete(reportHistory);
			}
		} catch (RuntimeException ex) {
		}
	}
}
