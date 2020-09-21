
package org.travice.notificators;

import org.travice.Context;
import org.travice.model.Event;
import org.travice.model.Position;
import org.travice.model.User;
import org.travice.notification.MessageException;
import org.travice.notification.NotificationFormatter;
import org.travice.sms.SmsManager;

public final class NotificatorSms extends Notificator {

    private final SmsManager smsManager;

    public NotificatorSms() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final String smsClass = Context.getConfig().getString("notificator.sms.manager.class", "");
        if (smsClass.length() > 0) {
            smsManager = (SmsManager) Class.forName(smsClass).newInstance();
        } else {
            smsManager = Context.getSmsManager();
        }
    }

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        final User user = Context.getPermissionsManager().getUser(userId);
        if (user.getPhone() != null) {
            Context.getStatisticsManager().registerSms();
            smsManager.sendMessageAsync(user.getPhone(),
                    NotificationFormatter.formatShortMessage(userId, event, position), false);
        }
    }

    @Override
    public void sendSync(long userId, Event event, Position position) throws MessageException, InterruptedException {
        final User user = Context.getPermissionsManager().getUser(userId);
        if (user.getPhone() != null) {
            Context.getStatisticsManager().registerSms();
            smsManager.sendMessageSync(user.getPhone(),
                    NotificationFormatter.formatShortMessage(userId, event, position), false);
        }
    }
}
