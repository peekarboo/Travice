
package org.travice.api;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.travice.Context;
import org.travice.api.resource.SessionResource;

public class AsyncSocketServlet extends WebSocketServlet {

    private static final long ASYNC_TIMEOUT = 10 * 60 * 1000;

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(Context.getConfig().getLong("web.timeout", ASYNC_TIMEOUT));
        factory.setCreator(new WebSocketCreator() {
            @Override
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
                if (req.getSession() != null) {
                    long userId = (Long) req.getSession().getAttribute(SessionResource.USER_ID_KEY);
                    return new AsyncSocket(userId);
                } else {
                    return null;
                }
            }
        });
    }

}
