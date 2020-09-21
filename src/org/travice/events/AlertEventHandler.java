
package org.travice.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.model.Event;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class AlertEventHandler extends BaseEventHandler {

    private final boolean ignoreDuplicateAlerts;

    public AlertEventHandler() {
        ignoreDuplicateAlerts = Context.getConfig().getBoolean("event.ignoreDuplicateAlerts");
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Object alarm = position.getAttributes().get(Position.KEY_ALARM);
        if (alarm != null) {
            boolean ignoreAlert = false;
            if (ignoreDuplicateAlerts) {
                Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
                if (lastPosition != null && alarm.equals(lastPosition.getAttributes().get(Position.KEY_ALARM))) {
                    ignoreAlert = true;
                }
            }
            if (!ignoreAlert) {
                Event event = new Event(Event.TYPE_ALARM, position.getDeviceId(), position.getId());
                event.set(Position.KEY_ALARM, (String) alarm);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }

}
