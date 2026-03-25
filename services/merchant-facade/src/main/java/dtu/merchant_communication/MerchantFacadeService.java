package dtu.merchant_communication;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

@Singleton
public class MerchantFacadeService {

	private static final long RESPONSE_TIMEOUT_SECONDS = 10;
	private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> completedPayments = new ConcurrentHashMap<>();


	private final MessageQueue queue;

	public MerchantFacadeService() {
		this(new RabbitMqQueue());
	}

	public MerchantFacadeService(MessageQueue q) {
		queue = q;
		q.addHandler("MerchantCreated", this::commandSendCreationSuccessfulResponse);
		q.addHandler("MerchantRegistrationFailed", this::commandSendCreationFailedResponse);
		q.addHandler("MerchantDeregistered", this::commandMerchantDeregistered);
		q.addHandler("MerchantFullReportReceived", this::commandSendMerchantFullReportResponse);
		q.addHandler("MerchantReportHistoryReceived", this::commandSendMerchantReportHistoryResponse);
		q.addHandler("PaymentCompleted", this::commandPaymentCompleted);
		q.addHandler("PaymentFailed", this::commandPaymentFailed);
		q.addHandler("ReportGenerationFailed", this::commandSendReportGenerationFailedResponse);
	}
	
	// @author Fabian
	private void commandSendReportGenerationFailedResponse(Event e) {
		String correlationId = e.getArgument(0, String.class);
		String reason = e.getArgument(1, String.class);
		CompletableFuture<String> response = pendingRequests.remove(correlationId);
		if (response != null) {
			response.completeExceptionally(new RuntimeException(reason));
		}
	}

	// @author Fabian
	private void commandSendCreationSuccessfulResponse(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			String newMerchantId = AccountId.fromEvent(e).toString();
			CompletableFuture<String> response = pendingRequests.remove(correlationId);
			if (response != null) {
				response.complete(newMerchantId);
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
			CompletableFuture<String> response = pendingRequests.remove(correlationId);
			if (response != null) {
				Response errorResponse = Response.status(Response.Status.CONFLICT)
						.entity(reason)
						.build();
				response.completeExceptionally(new WebApplicationException(errorResponse));
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Peter
	private void commandMerchantDeregistered(Event e) {
		try {
			String correlationId = e.getArgument(0, String.class);
			CompletableFuture<String> response = pendingRequests.remove(correlationId);
			if (response != null) {
				response.complete("Merchant deregistered");
			}
		} catch (RuntimeException ex) {
			// Ignore malformed events; caller will time out.
		}
	}

	// @author Christoffer
	public String registerMerchant(Merchant merchant) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);

		queue.publish(new Event("MerchantRegistrationRequested",
				correlationId,
				merchant.firstName(),
				merchant.lastName(),
				merchant.cpr(),
				merchant.merchantBankId()));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for MerchantRegistered", e);

		} catch (InterruptedException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);

		} catch (ExecutionException e) {
			pendingRequests.remove(correlationId);
			Throwable cause = e.getCause();
			if (cause instanceof WebApplicationException webException) {
				throw webException;
			}
			throw new RuntimeException(cause);
		}
	}

	// @author Frederik
	public String deregisterMerchant(String cid) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		queue.publish(new Event("MerchantDeRegistrationRequested", correlationId, cid));
		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for MerchantDeregistered", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Tobias
	public String payment(int amount, String merchantID, String token) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		String completed = completedPayments.remove(correlationId);
		if (completed != null) {
			response.complete(completed);
		}
		queue.publish(new Event("ValidateTokenRequest", correlationId, token)); // Token service
		queue.publish(new Event("ValidateMidRequest", correlationId, merchantID)); // Account manager
		queue.publish(new Event("PaymentRequest", correlationId, amount, merchantID, token)); // Payment service
		// queue.publish(new Event("PaymentReportRequest", correlationId, amount, merchantID, token)); // Report service - Doesn't get picked up

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for PaymentRequested", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Fabian
	public String getMerchantReport(String merchantId) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		queue.publish(new Event("GetMerchantReportRequested", correlationId, merchantId));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for GetMerchantReportResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Nikolaj
	private void commandSendMerchantFullReportResponse(Event event) {
		try {
			String correlationId = event.getArgument(0, String.class);
			String report = event.getArgument(1, String.class);
			CompletableFuture<String> response = pendingRequests.remove(correlationId);
			if (response != null) {
				response.complete(report);
			}
		} catch (RuntimeException ex) {

		}
	}

	// @author Peter
	public String getMerchantReportHistory(String merchantId) {
		String correlationId = UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		queue.publish(new Event("GetMerchantReportHistoryRequested", correlationId, merchantId));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for GetMerchantReportHistoryResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Tobias
	private void commandSendMerchantReportHistoryResponse(Event event) {
		try {
			String correlationId = event.getArgument(0, String.class);
			String reportHistory = event.getArgument(1, String.class);
			CompletableFuture<String> response = pendingRequests.remove(correlationId);
			if (response != null) {
				response.complete(reportHistory);
			}
		} catch (RuntimeException ex) {
		}
	}

	// public void handlePaymentFailed(Event e) {
	//
	// }

	// @author Fabian
	public void commandPaymentFailed(Event e) {
		String correlationId = e.getArgument(0, String.class);
		String reason = e.getArgument(1, String.class);
		CompletableFuture<String> response = pendingRequests.remove(correlationId);
		if (response != null) {
			response.complete("Payment failed: " + reason);
		} else {
			completedPayments.put(correlationId, "Payment failed: " + reason);
		}
	}

	// @author Christoffer
	public void commandPaymentCompleted(Event e) {
		String correlationID = e.getArgument(0, String.class);

		CompletableFuture<String> response = pendingRequests.remove(correlationID);
		if (response != null) {
			response.complete("Payment completed successfully");
		} else {
			completedPayments.put(correlationID, "Payment completed successfully");
		}
	}
}
