
package org.travice.database;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.travice.Context;
import org.travice.helper.Log;
import org.travice.model.Calendar;
import org.travice.model.Event;
import org.travice.model.Notification;
import org.travice.model.Position;
import org.travice.model.Typed;

public class NotificationManager extends ExtendedObjectManager<Notification> {

    private boolean geocodeOnRequest;

    public NotificationManager(DataManager dataManager) {
        super(dataManager, Notification.class);
        geocodeOnRequest = Context.getConfig().getBoolean("geocoder.onRequest");
    }

    private Set<Long> getEffectiveNotifications(long userId, long deviceId, Date time) {
        Set<Long> result = new HashSet<>();
        Set<Long> deviceNotifications = getAllDeviceItems(deviceId);
        for (long itemId : getUserItems(userId)) {
            if (getById(itemId).getAlways() || deviceNotifications.contains(itemId)) {
                long calendarId = getById(itemId).getCalendarId();
                Calendar calendar = calendarId != 0 ? Context.getCalendarManager().getById(calendarId) : null;
                if (calendar == null || calendar.checkMoment(time)) {
                    result.add(itemId);
                }
            }
        }
        return result;
    }

    public void updateEvent(Event event, Position position) {
        try {
            getDataManager().addObject(event);
        } catch (SQLException error) {
            Log.warning(error);
        }

        if (position != null && geocodeOnRequest && Context.getGeocoder() != null && position.getAddress() == null) {
            position.setAddress(Context.getGeocoder()
                    .getAddress(position.getLatitude(), position.getLongitude(), null));
        }

        long deviceId = event.getDeviceId();
        Set<Long> users = Context.getPermissionsManager().getDeviceUsers(deviceId);
        Set<Long> usersToForward = null;
        if (Context.getEventForwarder() != null) {
            usersToForward = new HashSet<>();
        }
        for (long userId : users) {
            if ((event.getGeofenceId() == 0
                    || Context.getGeofenceManager().checkItemPermission(userId, event.getGeofenceId()))
                    && (event.getMaintenanceId() == 0
                    || Context.getMaintenancesManager().checkItemPermission(userId, event.getMaintenanceId()))) {
                if (usersToForward != null) {
                    usersToForward.add(userId);
                }
                final Set<String> notificators = new HashSet<>();
                for (long notificationId : getEffectiveNotifications(userId, deviceId, event.getServerTime())) {
                    Notification notification = getById(notificationId);
                    if (getById(notificationId).getType().equals(event.getType())) {
                        notificators.addAll(notification.getNotificatorsTypes());
                    }
                }
                for (String notificator : notificators) {
                    Context.getNotificatorManager().getNotificator(notificator).sendAsync(userId, event, position);
                }
            }
        }
        if (Context.getEventForwarder() != null) {
            Context.getEventForwarder().forwardEvent(event, position, usersToForward);
        }
    }

    public void updateEvents(Map<Event, Position> events) {
        for (Entry<Event, Position> event : events.entrySet()) {
            updateEvent(event.getKey(), event.getValue());
        }
    }

    public Set<Typed> getAllNotificationTypes() {
        Set<Typed> types = new HashSet<>();
        Field[] fields = Event.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("TYPE_")) {
                try {
                    types.add(new Typed(field.get(null).toString()));
                } catch (IllegalArgumentException | IllegalAccessException error) {
                    Log.warning(error);
                }
            }
        }
        return types;
    }
}
