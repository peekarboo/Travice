
package org.travice.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.Context;
import org.travice.database.GeofenceManager;
import org.travice.model.Calendar;
import org.travice.model.Device;
import org.travice.model.Event;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class GeofenceEventHandler extends BaseEventHandler {

    private GeofenceManager geofenceManager;

    public GeofenceEventHandler() {
        geofenceManager = Context.getGeofenceManager();
    }

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Device device = Context.getIdentityManager().getById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!Context.getIdentityManager().isLatestPosition(position) || !position.getValid()) {
            return null;
        }

        List<Long> currentGeofences = geofenceManager.getCurrentDeviceGeofences(position);
        List<Long> oldGeofences = new ArrayList<>();
        if (device.getGeofenceIds() != null) {
            oldGeofences.addAll(device.getGeofenceIds());
        }
        List<Long> newGeofences = new ArrayList<>(currentGeofences);
        newGeofences.removeAll(oldGeofences);
        oldGeofences.removeAll(currentGeofences);

        device.setGeofenceIds(currentGeofences);

        Map<Event, Position> events = new HashMap<>();
        for (long geofenceId : newGeofences) {
            long calendarId = geofenceManager.getById(geofenceId).getCalendarId();
            Calendar calendar = calendarId != 0 ? Context.getCalendarManager().getById(calendarId) : null;
            if (calendar == null || calendar.checkMoment(position.getFixTime())) {
                Event event = new Event(Event.TYPE_GEOFENCE_ENTER, position.getDeviceId(), position.getId());
                event.setGeofenceId(geofenceId);
                events.put(event, position);
            }
        }
        for (long geofenceId : oldGeofences) {
            long calendarId = geofenceManager.getById(geofenceId).getCalendarId();
            Calendar calendar = calendarId != 0 ? Context.getCalendarManager().getById(calendarId) : null;
            if (calendar == null || calendar.checkMoment(position.getFixTime())) {
                Event event = new Event(Event.TYPE_GEOFENCE_EXIT, position.getDeviceId(), position.getId());
                event.setGeofenceId(geofenceId);
                events.put(event, position);
            }
        }
        return events;
    }
}
