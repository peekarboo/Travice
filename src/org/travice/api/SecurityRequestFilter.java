
package org.travice.api;

import org.travice.Context;
import org.travice.api.resource.SessionResource;
import org.travice.helper.DataConverter;
import org.travice.helper.Log;
import org.travice.model.User;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class SecurityRequestFilter implements ContainerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String BASIC_REALM = "Basic realm=\"api\"";
    public static final String X_REQUESTED_WITH = "X-Requested-With";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    public static String[] decodeBasicAuth(String auth) {
        auth = auth.replaceFirst("[B|b]asic ", "");
        byte[] decodedBytes = DataConverter.parseBase64(auth);
        if (decodedBytes != null && decodedBytes.length > 0) {
            return new String(decodedBytes, StandardCharsets.US_ASCII).split(":", 2);
        }
        return null;
    }

    @javax.ws.rs.core.Context
    private HttpServletRequest request;

    @javax.ws.rs.core.Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if (requestContext.getMethod().equals("OPTIONS")) {
            return;
        }

        SecurityContext securityContext = null;

        try {

            String authHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);
            if (authHeader != null) {

                try {
                    String[] auth = decodeBasicAuth(authHeader);
                    User user = Context.getPermissionsManager().login(auth[0], auth[1]);
                    if (user != null) {
                        Context.getStatisticsManager().registerRequest(user.getId());
                        securityContext = new UserSecurityContext(new UserPrincipal(user.getId()));
                    }
                } catch (SQLException e) {
                    throw new WebApplicationException(e);
                }

            } else if (request.getSession() != null) {

                Long userId = (Long) request.getSession().getAttribute(SessionResource.USER_ID_KEY);
                if (userId != null) {
                    Context.getPermissionsManager().checkUserEnabled(userId);
                    Context.getStatisticsManager().registerRequest(userId);
                    securityContext = new UserSecurityContext(new UserPrincipal(userId));
                }

            }

        } catch (SecurityException e) {
            Log.warning(e);
        }

        if (securityContext != null) {
            requestContext.setSecurityContext(securityContext);
        } else {
            Method method = resourceInfo.getResourceMethod();
            if (!method.isAnnotationPresent(PermitAll.class)) {
                Response.ResponseBuilder responseBuilder = Response.status(Response.Status.UNAUTHORIZED);
                if (!XML_HTTP_REQUEST.equals(request.getHeader(X_REQUESTED_WITH))) {
                    responseBuilder.header(WWW_AUTHENTICATE, BASIC_REALM);
                }
                throw new WebApplicationException(responseBuilder.build());
            }
        }

    }

}
