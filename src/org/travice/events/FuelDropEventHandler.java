
package org.travice.events;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.model.Device;
import org.travice.model.Event;
import org.travice.model.Position;

import java.util.Collections;
import java.util.Map;

@ChannelHandler.Sharable
public class FuelDropEventHandler extends BaseEventHandler {

    public static final String ATTRIBUTE_FUEL_DROP_THRESHOLD = "fuelDropThreshold";

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {

        Device device = Context.getIdentityManager().getById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!Context.getIdentityManager().isLatestPosition(position)) {
            return null;
        }

        double fuelDropThreshold = Context.getDeviceManager()
                .lookupAttributeDouble(device.getId(), ATTRIBUTE_FUEL_DROP_THRESHOLD, 0, false);

        if (fuelDropThreshold > 0) {
            Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
            if (position.getAttributes().containsKey(Position.KEY_FUEL_LEVEL)
                    && lastPosition != null && lastPosition.getAttributes().containsKey(Position.KEY_FUEL_LEVEL)) {

                double drop = lastPosition.getDouble(Position.KEY_FUEL_LEVEL)
                        - position.getDouble(Position.KEY_FUEL_LEVEL);
                if (drop >= fuelDropThreshold) {
                    Event event = new Event(Event.TYPE_DEVICE_FUEL_DROP, position.getDeviceId(), position.getId());
                    event.set(ATTRIBUTE_FUEL_DROP_THRESHOLD, fuelDropThreshold);
                    return Collections.singletonMap(event, position);
                }
            }
        }

        return null;
    }

}
