
package org.travice.notification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.travice.Context;
import org.travice.helper.Log;
import org.travice.model.Typed;
import org.travice.notificators.NotificatorNull;
import org.travice.notificators.Notificator;

public final class NotificatorManager {

    private static final String DEFAULT_WEB_NOTIFICATOR = "org.travice.notificators.NotificatorWeb";
    private static final String DEFAULT_MAIL_NOTIFICATOR = "org.travice.notificators.NotificatorMail";
    private static final String DEFAULT_SMS_NOTIFICATOR = "org.travice.notificators.NotificatorSms";

    private final Map<String, Notificator> notificators = new HashMap<>();
    private static final Notificator NULL_NOTIFICATOR = new NotificatorNull();

    public NotificatorManager() {
        final String[] types = Context.getConfig().getString("notificator.types", "").split(",");
        for (String type : types) {
            String defaultNotificator = "";
            switch (type) {
                case "web":
                    defaultNotificator = DEFAULT_WEB_NOTIFICATOR;
                    break;
                case "mail":
                    defaultNotificator = DEFAULT_MAIL_NOTIFICATOR;
                    break;
                case "sms":
                    defaultNotificator = DEFAULT_SMS_NOTIFICATOR;
                    break;
                default:
                    break;
            }
            final String className = Context.getConfig()
                    .getString("notificator." + type + ".class", defaultNotificator);
            try {
                notificators.put(type, (Notificator) Class.forName(className).newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                Log.error("Unable to load notificator class for " + type + " " + className + " " + e.getMessage());
            }
        }
    }

    public Notificator getNotificator(String type) {
        final Notificator notificator = notificators.get(type);
        if (notificator == null) {
            Log.error("No notificator configured for type : " + type);
            return NULL_NOTIFICATOR;
        }
        return notificator;
    }

    public Set<Typed> getAllNotificatorTypes() {
        Set<Typed> result = new HashSet<>();
        for (String notificator : notificators.keySet()) {
            result.add(new Typed(notificator));
        }
        return result;
    }

}
