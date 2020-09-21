
package org.travice.web;

import org.h2.server.web.ConnectionInfo;
import org.h2.server.web.WebServlet;
import org.travice.Context;
import org.travice.helper.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConsoleServlet extends WebServlet {

    @Override
    public void init() {
        super.init();

        try {
            Field field = WebServlet.class.getDeclaredField("server");
            field.setAccessible(true);
            org.h2.server.web.WebServer server = (org.h2.server.web.WebServer) field.get(this);

            ConnectionInfo connectionInfo = new ConnectionInfo("Travice|"
                    + Context.getConfig().getString("database.driver") + "|"
                    + Context.getConfig().getString("database.url") + "|"
                    + Context.getConfig().getString("database.user"));

            Method method;

            method = org.h2.server.web.WebServer.class.getDeclaredMethod("updateSetting", ConnectionInfo.class);
            method.setAccessible(true);
            method.invoke(server, connectionInfo);

            method = org.h2.server.web.WebServer.class.getDeclaredMethod("setAllowOthers", boolean.class);
            method.setAccessible(true);
            method.invoke(server, true);

        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.warning(e);
        }
    }

}
