
package org.travice.notificators;

import org.travice.helper.Log;
import org.travice.model.Event;
import org.travice.model.Position;
import org.travice.notification.MessageException;

public abstract class Notificator {

    public void sendAsync(final long userId, final Event event, final Position position) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    sendSync(userId, event, position);
                } catch (MessageException | InterruptedException error) {
                    Log.warning(error);
                }
            }
        }).start();
    }

    public abstract void sendSync(long userId, Event event, Position position)
        throws MessageException, InterruptedException;

}
