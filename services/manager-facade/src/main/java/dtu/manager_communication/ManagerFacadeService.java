package dtu.manager_communication;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.UUID;

import jakarta.inject.Singleton;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

@Singleton
public class ManagerFacadeService {

	private static final long RESPONSE_TIMEOUT_SECONDS = 10;
	private static final UUID MANAGER_ID = UUID.randomUUID(); // Fixed UUID for the manager account
	private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

	private final MessageQueue queue;

	public ManagerFacadeService() {
		this(new RabbitMqQueue());
	}

	public ManagerFacadeService(MessageQueue q) {
		this.queue = q;
		q.addHandler("FullReportRequested", this::commandFullReportRequested);
		q.addHandler("MerchantFullReportReceived", this::commandSendMerchantReportResponse);
		q.addHandler("CustomerFullReportReceived", this::commandSendCustomerReportResponse);

		q.addHandler("ManagerReportHistoryReceived", this::commandSendManagerReportHistoryResponse);
		q.addHandler("MerchantReportHistoryReceived", this::commandSendMerchantReportHistoryResponse);
		q.addHandler("CustomerReportHistoryReceived", this::commandSendCustomerReportHistoryResponse);

		q.addHandler("ReportGenerationFailed", this::commandSendReportGenerationFailedResponse);
	}
	private void commandSendReportGenerationFailedResponse(Event e) {
		String correlationId = e.getArgument(0, String.class);
		String reason = e.getArgument(1, String.class);
		CompletableFuture<String> response = pendingRequests.remove(correlationId);
		if (response != null) {
			response.completeExceptionally(new RuntimeException(reason));
		}
	}

	// @author Frederik
	public String requestAllReport() {
		String correlationId = java.util.UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		queue.publish(new Event("GetAllReportRequested", correlationId, MANAGER_ID));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for GetAllReportResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Frederik
	public String requestMerchantReport(String merchantId) {
		String correlationId = java.util.UUID.randomUUID().toString();
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


	public String requestCustomerReport(String customerId) {
		String correlationId = java.util.UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		queue.publish(new Event("GetCustomerReportRequested", correlationId, customerId));
		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for GetCustomerReportResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Ksawery
	public String requestReportHistory() {
		String correlationId = java.util.UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		queue.publish(new Event("GetManagerReportHistoryRequested", correlationId, MANAGER_ID));

		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for GetManagerReportHistoryResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Ksawery
	public String requestMerchantReportHistory(String merchantId) {
		String correlationId = java.util.UUID.randomUUID().toString();
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
	public String requestCustomerReportHistory(String customerId) {
		String correlationId = java.util.UUID.randomUUID().toString();
		CompletableFuture<String> response = new CompletableFuture<>();
		pendingRequests.put(correlationId, response);
		queue.publish(new Event("GetCustomerReportHistoryRequested", correlationId, customerId));
		try {
			return response.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			pendingRequests.remove(correlationId);
			throw new RuntimeException("Timed out waiting for GetCustomerReportHistoryResponse", e);
		} catch (InterruptedException | ExecutionException e) {
			pendingRequests.remove(correlationId);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
	}

	// @author Frederik
	private void commandFullReportRequested(Event event) {
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

	// @author Christoffer
	private void commandSendMerchantReportResponse(Event event) {
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


	private void commandSendCustomerReportResponse(Event event) {
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
	
	// @author Christoffer
	private void commandSendManagerReportHistoryResponse(Event event) {
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

	// @author Peter
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
	private void commandSendCustomerReportHistoryResponse(Event event) {
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

}
