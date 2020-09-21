
package org.travice.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.model.Event;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class DriverEventHandler extends BaseEventHandler {

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        if (!Context.getIdentityManager().isLatestPosition(position)) {
            return null;
        }
        String driverUniqueId = position.getString(Position.KEY_DRIVER_UNIQUE_ID);
        if (driverUniqueId != null) {
            String oldDriverUniqueId = null;
            Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
            if (lastPosition != null) {
                oldDriverUniqueId = lastPosition.getString(Position.KEY_DRIVER_UNIQUE_ID);
            }
            if (!driverUniqueId.equals(oldDriverUniqueId)) {
                Event event = new Event(Event.TYPE_DRIVER_CHANGED, position.getDeviceId(), position.getId());
                event.set(Position.KEY_DRIVER_UNIQUE_ID, driverUniqueId);
                return Collections.singletonMap(event, position);
            }
        }
        return null;
    }

}
