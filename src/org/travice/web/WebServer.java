
package org.travice.web;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.travice.Config;
import org.travice.Context;
import org.travice.api.AsyncSocketServlet;
import org.travice.api.CorsResponseFilter;
import org.travice.api.MediaFilter;
import org.travice.api.ObjectMapperProvider;
import org.travice.api.ResourceErrorHandler;
import org.travice.api.SecurityRequestFilter;
import org.travice.api.resource.ServerResource;
import org.travice.helper.Log;

import javax.naming.InitialContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.EnumSet;

public class WebServer {

    private Server server;
    private final Config config;
    private final DataSource dataSource;
    private final HandlerList handlers = new HandlerList();
    private final SessionManager sessionManager;

    private void initServer() {

        String address = config.getString("web.address");
        int port = config.getInteger("web.port", 8082);
        if (address == null) {
            server = new Server(port);
        } else {
            server = new Server(new InetSocketAddress(address, port));
        }
    }

    public WebServer(Config config, DataSource dataSource) {
        this.config = config;
        this.dataSource = dataSource;

        sessionManager = new HashSessionManager();
        int sessionTimeout = config.getInteger("web.sessionTimeout");
        if (sessionTimeout != 0) {
            sessionManager.setMaxInactiveInterval(sessionTimeout);
        }

        initServer();
        initApi();
        if (config.getBoolean("web.console")) {
            initConsole();
        }
        switch (config.getString("web.type", "new")) {
            case "old":
                initOldWebApp();
                break;
            default:
                initWebApp();
                break;
        }
        initClientProxy();
        server.setHandler(handlers);

        server.addBean(new ErrorHandler() {
            @Override
            protected void handleErrorPage(
                    HttpServletRequest request, Writer writer, int code, String message) throws IOException {
                writer.write("<!DOCTYPE<html><head><title>Error</title></head><html><body>"
                        + code + " - " + HttpStatus.getMessage(code) + "</body></html>");
            }
        }, false);
    }

    private void initClientProxy() {
        int port = Context.getConfig().getInteger("osmand.port");
        if (port != 0) {
            ServletContextHandler servletHandler = new ServletContextHandler() {
                @Override
                public void doScope(
                        String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                        throws IOException, ServletException {
                    if (target.equals("/") && request.getMethod().equals(HttpMethod.POST.asString())) {
                        super.doScope(target, baseRequest, request, response);
                    }
                }
            };
            ServletHolder servletHolder = new ServletHolder(new AsyncProxyServlet.Transparent());
            servletHolder.setInitParameter("proxyTo", "http://localhost:" + port);
            servletHandler.addServlet(servletHolder, "/");
            handlers.addHandler(servletHandler);
        }
    }

    private void initWebApp() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(config.getString("web.path"));
        if (config.getBoolean("web.debug")) {
            resourceHandler.setWelcomeFiles(new String[] {"debug.html", "index.html"});
            resourceHandler.setMinMemoryMappedContentLength(-1); // avoid locking files on Windows
        } else {
            String cache = config.getString("web.cacheControl");
            if (cache != null && !cache.isEmpty()) {
                resourceHandler.setCacheControl(cache);
            }
            resourceHandler.setWelcomeFiles(new String[] {"release.html", "index.html"});
        }
        handlers.addHandler(resourceHandler);
    }

    private void initOldWebApp() {
        try {
            javax.naming.Context context = new InitialContext();
            context.bind("java:/DefaultDS", dataSource);
        } catch (Exception error) {
            Log.warning(error);
        }

        WebAppContext app = new WebAppContext();
        app.setContextPath("/");
        app.getSessionHandler().setSessionManager(sessionManager);
        app.setWar(config.getString("web.application"));
        handlers.addHandler(app);
    }

    private void initApi() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/api");
        servletHandler.getSessionHandler().setSessionManager(sessionManager);

        servletHandler.addServlet(new ServletHolder(new AsyncSocketServlet()), "/socket");

        if (config.hasKey("media.path")) {
            ServletHolder servletHolder = new ServletHolder("media", DefaultServlet.class);
            servletHolder.setInitParameter("resourceBase", config.getString("media.path"));
            servletHolder.setInitParameter("dirAllowed", config.getString("media.dirAllowed", "false"));
            servletHolder.setInitParameter("pathInfoOnly", "true");
            servletHandler.addServlet(servletHolder, "/media/*");
            servletHandler.addFilter(MediaFilter.class, "/media/*", EnumSet.allOf(DispatcherType.class));
        }

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.registerClasses(JacksonFeature.class, ObjectMapperProvider.class, ResourceErrorHandler.class);
        resourceConfig.registerClasses(SecurityRequestFilter.class, CorsResponseFilter.class);
        resourceConfig.packages(ServerResource.class.getPackage().getName());
        servletHandler.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");

        handlers.addHandler(servletHandler);
    }

    private void initConsole() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/console");
        servletHandler.addServlet(new ServletHolder(new ConsoleServlet()), "/*");
        handlers.addHandler(servletHandler);
    }

    public void start() {
        try {
            server.start();
        } catch (Exception error) {
            Log.warning(error);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception error) {
            Log.warning(error);
        }
    }

}
