
package org.travice.notificators;

import org.travice.helper.Log;
import org.travice.model.Event;
import org.travice.model.Position;

public final class NotificatorNull extends Notificator {

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        Log.warning("You are using null notificatior, please check your configuration, notification not sent");
    }

    @Override
    public void sendSync(long userId, Event event, Position position) {
        Log.warning("You are using null notificatior, please check your configuration, notification not sent");
    }
}
