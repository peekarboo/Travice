
package org.travice.notificators;

import org.travice.Context;
import org.travice.model.Event;
import org.travice.model.Position;

public final class NotificatorWeb extends Notificator {

    @Override
    public void sendSync(long userId, Event event, Position position) {
        Context.getConnectionManager().updateEvent(userId, event);
    }

}
