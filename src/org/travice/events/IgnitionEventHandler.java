
package org.travice.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.model.Device;
import org.travice.model.Event;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class IgnitionEventHandler extends BaseEventHandler {

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Device device = Context.getIdentityManager().getById(position.getDeviceId());
        if (device == null || !Context.getIdentityManager().isLatestPosition(position)) {
            return null;
        }

        Map<Event, Position> result = null;

        if (position.getAttributes().containsKey(Position.KEY_IGNITION)) {
            boolean ignition = position.getBoolean(Position.KEY_IGNITION);

            Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
            if (lastPosition != null && lastPosition.getAttributes().containsKey(Position.KEY_IGNITION)) {
                boolean oldIgnition = lastPosition.getBoolean(Position.KEY_IGNITION);

                if (ignition && !oldIgnition) {
                    result = Collections.singletonMap(
                            new Event(Event.TYPE_IGNITION_ON, position.getDeviceId(), position.getId()), position);
                } else if (!ignition && oldIgnition) {
                    result = Collections.singletonMap(
                            new Event(Event.TYPE_IGNITION_OFF, position.getDeviceId(), position.getId()), position);
                }
            }
        }
        return result;
    }

}
