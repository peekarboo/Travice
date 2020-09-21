
package org.travice;

import org.travice.helper.Log;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Locale;

public final class Main {

    private static final long CLEAN_PERIOD = 24 * 60 * 60 * 1000;

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);

        Context.init(args);
        Log.info("Starting server...");

        Context.getServerManager().start();
        if (Context.getWebServer() != null) {
            Context.getWebServer().start();
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Context.getDataManager().clearHistory();
                } catch (SQLException error) {
                    Log.warning(error);
                }
            }
        }, 0, CLEAN_PERIOD);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Log.info("Shutting down server...");

                if (Context.getWebServer() != null) {
                    Context.getWebServer().stop();
                }
                Context.getServerManager().stop();
            }
        });
    }

}
