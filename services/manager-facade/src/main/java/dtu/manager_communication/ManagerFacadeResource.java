package dtu.manager_communication;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/manager")
@Produces(MediaType.APPLICATION_JSON)
public class ManagerFacadeResource {

	@Inject
	ManagerFacadeService service;

	@Path("/payments")
	@GET
	// @author Frederik
	public String getAllReport() {
		return service.requestAllReport();
	}

	@Path("/payments/merchant/{merchantId}")
	@GET
	// @author Nikolaj
	public String getMerchantReport(@jakarta.ws.rs.PathParam("merchantId") String merchantId) {
		return service.requestMerchantReport(merchantId);
	}

	@Path("/payments/customer/{customerId}")
	@GET
	// @author Fabian
	public String getCustomerReport(@jakarta.ws.rs.PathParam("customerId") String customerId) {
		return service.requestCustomerReport(customerId);
	}


	@Path("/history")
	@GET
	// @author Christoffer
	public String getReportHistory() {
		return service.requestReportHistory();
	}

	@Path("/history/merchant/{merchantId}")
	@GET
	// @author Christoffer
	public String getMerchantReportHistory(@jakarta.ws.rs.PathParam("merchantId") String merchantId) {
		return service.requestMerchantReportHistory(merchantId);
	}

	@Path("/history/customer/{customerId}")
	@GET
	// @author Frederik
	public String getCustomerReportHistory(@jakarta.ws.rs.PathParam("customerId") String customerId) {
		return service.requestCustomerReportHistory(customerId);
	}
}
