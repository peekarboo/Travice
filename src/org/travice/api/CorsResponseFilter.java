
package org.travice.api;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.travice.Context;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

public class CorsResponseFilter implements ContainerResponseFilter {

    private static final String ORIGIN_ALL = "*";
    private static final String HEADERS_ALL = "origin, content-type, accept, authorization";
    private static final String METHODS_ALL = "GET, POST, PUT, DELETE, OPTIONS";

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString())) {
            response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(), HEADERS_ALL);
        }

        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString())) {
            response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), true);
        }

        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString())) {
            response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), METHODS_ALL);
        }

        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString())) {
            String origin = request.getHeaderString(HttpHeaderNames.ORIGIN.toString());
            String allowed = Context.getConfig().getString("web.origin");

            if (origin == null) {
                response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), ORIGIN_ALL);
            } else if (allowed == null || allowed.equals(ORIGIN_ALL) || allowed.contains(origin)) {
                response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), origin);
            }
        }
    }

}
