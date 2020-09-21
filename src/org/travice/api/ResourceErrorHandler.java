
package org.travice.api;

import org.travice.helper.Log;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ResourceErrorHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof WebApplicationException) {
            WebApplicationException exception = (WebApplicationException) e;
            String message;
            if (exception.getCause() != null) {
                message = Log.exceptionStack(exception.getCause());
            } else {
                message = Log.exceptionStack(exception);
            }
            return Response.fromResponse(exception.getResponse()).entity(message).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(Log.exceptionStack(e)).build();
        }
    }

}
