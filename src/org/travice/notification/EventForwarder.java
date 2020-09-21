
package org.travice.notification;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.travice.Context;
import org.travice.model.Device;
import org.travice.model.Event;
import org.travice.model.Geofence;
import org.travice.model.Maintenance;
import org.travice.model.Position;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Invocation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class EventForwarder {

    private final String url;
    private final String header;

    public EventForwarder() {
        url = Context.getConfig().getString("event.forward.url", "http://localhost/");
        header = Context.getConfig().getString("event.forward.header");
    }

    private static final String KEY_POSITION = "position";
    private static final String KEY_EVENT = "event";
    private static final String KEY_GEOFENCE = "geofence";
    private static final String KEY_DEVICE = "device";
    private static final String KEY_MAINTENANCE = "maintenance";
    private static final String KEY_USERS = "users";

    public final void forwardEvent(Event event, Position position, Set<Long> users) {

        Invocation.Builder requestBuilder = Context.getClient().target(url).request();

        if (header != null && !header.isEmpty()) {
            for (Map.Entry<String, String> entry : splitKeyValues(header, ":").entries()) {
                requestBuilder = requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        executeRequest(event, position, users, requestBuilder.async());
    }

    protected MultiValuedMap<String, String> splitKeyValues(String params, String separator) {
        MultiValuedMap<String, String> data = new ArrayListValuedHashMap<>();
        for (String line: params.split("\\r?\\n")) {
            String[] values = line.split(separator, 2);
            if (values.length == 2) {
                data.put(values[0].trim(), values[1].trim());
            }
        }
        return data;
    }

    protected Map<String, Object> preparePayload(Event event, Position position, Set<Long> users) {
        Map<String, Object> data = new HashMap<>();
        data.put(KEY_EVENT, event);
        if (position != null) {
            data.put(KEY_POSITION, position);
        }
        Device device = Context.getIdentityManager().getById(event.getDeviceId());
        if (device != null) {
            data.put(KEY_DEVICE, device);
        }
        if (event.getGeofenceId() != 0) {
            Geofence geofence = Context.getGeofenceManager().getById(event.getGeofenceId());
            if (geofence != null) {
                data.put(KEY_GEOFENCE, geofence);
            }
        }
        if (event.getMaintenanceId() != 0) {
            Maintenance maintenance = Context.getMaintenancesManager().getById(event.getMaintenanceId());
            if (maintenance != null) {
                data.put(KEY_MAINTENANCE, maintenance);
            }
        }
        data.put(KEY_USERS, Context.getUsersManager().getItems(users));
        return data;
    }

    protected abstract void executeRequest(
            Event event, Position position, Set<Long> users, AsyncInvoker invoker);

}
