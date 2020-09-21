
package org.travice.database;

import java.util.ArrayList;
import java.util.List;

import org.travice.Context;
import org.travice.model.Device;
import org.travice.model.Geofence;
import org.travice.model.Position;

public class GeofenceManager extends ExtendedObjectManager<Geofence> {

    public GeofenceManager(DataManager dataManager) {
        super(dataManager, Geofence.class);
    }

    @Override
    public final void refreshExtendedPermissions() {
        super.refreshExtendedPermissions();
        recalculateDevicesGeofences();
    }

    public List<Long> getCurrentDeviceGeofences(Position position) {
        List<Long> result = new ArrayList<>();
        for (long geofenceId : getAllDeviceItems(position.getDeviceId())) {
            Geofence geofence = getById(geofenceId);
            if (geofence != null && geofence.getGeometry()
                    .containsPoint(position.getLatitude(), position.getLongitude())) {
                result.add(geofenceId);
            }
        }
        return result;
    }

    public void recalculateDevicesGeofences() {
        for (Device device : Context.getDeviceManager().getAllDevices()) {
            List<Long> deviceGeofenceIds = device.getGeofenceIds();
            if (deviceGeofenceIds == null) {
                deviceGeofenceIds = new ArrayList<>();
            } else {
                deviceGeofenceIds.clear();
            }
            Position lastPosition = Context.getIdentityManager().getLastPosition(device.getId());
            if (lastPosition != null && getAllDeviceItems(device.getId()) != null) {
                deviceGeofenceIds.addAll(getCurrentDeviceGeofences(lastPosition));
            }
            device.setGeofenceIds(deviceGeofenceIds);
        }
    }

}
