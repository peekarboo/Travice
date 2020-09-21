
package org.travice.api;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.travice.Context;
import org.travice.api.resource.SessionResource;
import org.travice.helper.Log;
import org.travice.model.Device;

public class MediaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            HttpSession session = ((HttpServletRequest) request).getSession(false);
            Long userId = null;
            if (session != null) {
                userId = (Long) session.getAttribute(SessionResource.USER_ID_KEY);
                if (userId != null) {
                    Context.getPermissionsManager().checkUserEnabled(userId);
                    Context.getStatisticsManager().registerRequest(userId);
                }
            }
            if (userId == null) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String path = ((HttpServletRequest) request).getPathInfo();
            String[] parts = path.split("/");
            if (parts.length < 2 || parts.length == 2 && !path.endsWith("/")) {
                Context.getPermissionsManager().checkAdmin(userId);
            } else {
                Device device = Context.getDeviceManager().getByUniqueId(parts[1]);
                if (device != null) {
                    Context.getPermissionsManager().checkDevice(userId, device.getId());
                } else {
                    httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            chain.doFilter(request, response);
        } catch (SecurityException e) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().println(Log.exceptionStack(e));
        } catch (SQLException e) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getWriter().println(Log.exceptionStack(e));
        }
    }

    @Override
    public void destroy() {
    }

}
