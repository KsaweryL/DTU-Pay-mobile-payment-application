package dtu.merchant_communication;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/merchant")
public class MerchantFacadeResource {

	@Inject
	MerchantFacadeService service;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN) 
	// @author Peter
	public String registerMerchant(Merchant m) {
		return service.registerMerchant(m);
	}

	@Path("payment")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN) 
	// @author Tobias
	public String payment(PaymentRequest request) {
		return service.payment(request.amount(), request.merchantID(), request.token());
	}

	@Path("{merchantId}")
	@DELETE
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN) 
	// @author Nikolaj
	public String deregisterMerchant(@PathParam("merchantId") String p) {
		return service.deregisterMerchant(p);
	}

	@Path("{merchantId}/report")
	@GET
	// @author Fabian
	@Produces(MediaType.APPLICATION_JSON)
	public String getMerchantReport(@PathParam("merchantId") String merchantId) {
		return service.getMerchantReport(merchantId);
	}

	@Path("{merchantId}/report/history")
	@GET
	//@author Peter 
	@Produces(MediaType.APPLICATION_JSON)
	public String getMerchantReportHistory(@PathParam("merchantId") String merchantId) {
		return service.getMerchantReportHistory(merchantId);
	}

}
