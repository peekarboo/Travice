
package org.travice.api.resource;

import org.travice.Context;
import org.travice.api.BaseObjectResource;
import org.travice.database.DeviceManager;
import org.travice.helper.LogAction;
import org.travice.model.Device;
import org.travice.model.DeviceTotalDistance;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceResource extends BaseObjectResource<Device> {

    public DeviceResource() {
        super(Device.class);
    }

    @GET
    public Collection<Device> get(
            @QueryParam("all") boolean all, @QueryParam("userId") long userId,
            @QueryParam("uniqueId") List<String> uniqueIds,
            @QueryParam("id") List<Long> deviceIds) throws SQLException {
        DeviceManager deviceManager = Context.getDeviceManager();
        Set<Long> result = null;
        if (all) {
            if (Context.getPermissionsManager().getUserAdmin(getUserId())) {
                result = deviceManager.getAllItems();
            } else {
                Context.getPermissionsManager().checkManager(getUserId());
                result = deviceManager.getManagedItems(getUserId());
            }
        } else if (uniqueIds.isEmpty() && deviceIds.isEmpty()) {
            if (userId == 0) {
                userId = getUserId();
            }
            Context.getPermissionsManager().checkUser(getUserId(), userId);
            if (Context.getPermissionsManager().getUserAdmin(getUserId())) {
                result = deviceManager.getAllUserItems(userId);
            } else {
                result = deviceManager.getUserItems(userId);
            }
        } else {
            result = new HashSet<>();
            for (String uniqueId : uniqueIds) {
                Device device = deviceManager.getByUniqueId(uniqueId);
                Context.getPermissionsManager().checkDevice(getUserId(), device.getId());
                result.add(device.getId());
            }
            for (Long deviceId : deviceIds) {
                Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
                result.add(deviceId);
            }
        }
        return deviceManager.getItems(result);
    }

    @Path("{id}/distance")
    @PUT
    public Response updateTotalDistance(DeviceTotalDistance entity) throws SQLException {
        if (!Context.getPermissionsManager().getUserAdmin(getUserId())) {
            Context.getPermissionsManager().checkManager(getUserId());
            Context.getPermissionsManager().checkPermission(Device.class, getUserId(), entity.getDeviceId());
        }
        Context.getDeviceManager().resetTotalDistance(entity);
        LogAction.resetTotalDistance(getUserId(), entity.getDeviceId());
        return Response.noContent().build();
    }

}
