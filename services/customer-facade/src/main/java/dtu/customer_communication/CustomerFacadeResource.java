package dtu.customer_communication;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/customer")
public class CustomerFacadeResource {

	@Inject
	CustomerFacadeService service;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN) 
	// @author Ksawery
	public String registerCustomer(Customer c) {
		return service.registerCustomer(c);
	}

	@Path("{customerId}")  
	@DELETE
	@Produces(MediaType.TEXT_PLAIN) 
	// @author Ksawery
	public String deregisterCustomer(@PathParam("customerId") String cid) {
		return service.deregisterCustomer(cid);
	}

	@Path("{customerId}/tokens") 
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN) 
	// @author Nikolaj
	public String getTokens(@PathParam("customerId") String customerId, @QueryParam("count") int numberOfTokens) {
		return service.getTokens(customerId, numberOfTokens);
	}

	@Path("{customerId}/report") 
	@GET
	// @author Peter 
	@Produces(MediaType.APPLICATION_JSON)
	public String getCustomerReport(@PathParam("customerId") String customerId) {
		return service.getCustomerReport(customerId);
	}

	@Path("{customerId}/report/history")
	@GET
	// @author Tobias 
	@Produces(MediaType.APPLICATION_JSON)
	public String getCustomerReportHistory(@PathParam("customerId") String customerId) {
		return service.getCustomerReportHistory(customerId);
	}
}
