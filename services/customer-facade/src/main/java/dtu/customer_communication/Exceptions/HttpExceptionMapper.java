package dtu.customer_communication.Exceptions;

import java.util.Set;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class HttpExceptionMapper implements ExceptionMapper<HttpException> {

    @Override
    public Response toResponse(HttpException ex) {
        String msg = ex.getMessage();

        Set<String> conflictMessages = Set.of(
            "existing tokens",
            "already exists"
        );
        if (conflictMessages.stream().anyMatch(msg::contains)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(msg)
                    .build();
        }

        if (msg.contains("not found")) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(msg)
                    .build();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(msg)
                .build();
    }
}
