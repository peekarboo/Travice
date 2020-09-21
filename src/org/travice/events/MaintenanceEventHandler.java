
package org.travice.events;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.model.Event;
import org.travice.model.Maintenance;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class MaintenanceEventHandler extends BaseEventHandler {

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        if (Context.getIdentityManager().getById(position.getDeviceId()) == null
                || !Context.getIdentityManager().isLatestPosition(position)) {
            return null;
        }

        Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        if (lastPosition == null) {
            return null;
        }

        Map<Event, Position> events = new HashMap<>();
        for (long maintenanceId : Context.getMaintenancesManager().getAllDeviceItems(position.getDeviceId())) {
            Maintenance maintenance = Context.getMaintenancesManager().getById(maintenanceId);
            if (maintenance.getPeriod() != 0) {
                double oldValue = lastPosition.getDouble(maintenance.getType());
                double newValue = position.getDouble(maintenance.getType());
                if (oldValue != 0.0 && newValue != 0.0
                        && (long) ((oldValue - maintenance.getStart()) / maintenance.getPeriod())
                        < (long) ((newValue - maintenance.getStart()) / maintenance.getPeriod())) {
                    Event event = new Event(Event.TYPE_MAINTENANCE, position.getDeviceId(), position.getId());
                    event.setMaintenanceId(maintenanceId);
                    event.set(maintenance.getType(), newValue);
                    events.put(event, position);
                }
            }
        }

        return events;
    }

}
