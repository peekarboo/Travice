
package org.travice.api;

import java.sql.SQLException;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.travice.Context;
import org.travice.database.BaseObjectManager;
import org.travice.database.ExtendedObjectManager;
import org.travice.database.ManagableObjects;
import org.travice.database.SimpleObjectManager;
import org.travice.helper.LogAction;
import org.travice.model.BaseModel;
import org.travice.model.Calendar;
import org.travice.model.Command;
import org.travice.model.Device;
import org.travice.model.Group;
import org.travice.model.GroupedModel;
import org.travice.model.ScheduledModel;
import org.travice.model.User;

public abstract class BaseObjectResource<T extends BaseModel> extends BaseResource {

    private Class<T> baseClass;

    public BaseObjectResource(Class<T> baseClass) {
        this.baseClass = baseClass;
    }

    protected final Class<T> getBaseClass() {
        return baseClass;
    }

    protected final Set<Long> getSimpleManagerItems(BaseObjectManager<T> manager, boolean all,  long userId) {
        Set<Long> result = null;
        if (all) {
            if (Context.getPermissionsManager().getUserAdmin(getUserId())) {
                result = manager.getAllItems();
            } else {
                Context.getPermissionsManager().checkManager(getUserId());
                result = ((ManagableObjects) manager).getManagedItems(getUserId());
            }
        } else {
            if (userId == 0) {
                userId = getUserId();
            }
            Context.getPermissionsManager().checkUser(getUserId(), userId);
            result = ((ManagableObjects) manager).getUserItems(userId);
        }
        return result;
    }

    @POST
    public Response add(T entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        if (baseClass.equals(Device.class)) {
            Context.getPermissionsManager().checkDeviceReadonly(getUserId());
            Context.getPermissionsManager().checkDeviceLimit(getUserId());
        } else if (baseClass.equals(Command.class)) {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        } else if (entity instanceof GroupedModel && ((GroupedModel) entity).getGroupId() != 0) {
            Context.getPermissionsManager().checkPermission(
                    Group.class, getUserId(), ((GroupedModel) entity).getGroupId());
        } else if (entity instanceof ScheduledModel && ((ScheduledModel) entity).getCalendarId() != 0) {
            Context.getPermissionsManager().checkPermission(
                    Calendar.class, getUserId(), ((ScheduledModel) entity).getCalendarId());
        }

        BaseObjectManager<T> manager = Context.getManager(baseClass);
        manager.addItem(entity);
        LogAction.create(getUserId(), entity);

        Context.getDataManager().linkObject(User.class, getUserId(), baseClass, entity.getId(), true);
        LogAction.link(getUserId(), User.class, getUserId(), baseClass, entity.getId());

        if (manager instanceof SimpleObjectManager) {
            ((SimpleObjectManager<T>) manager).refreshUserItems();
        } else if (baseClass.equals(Group.class) || baseClass.equals(Device.class)) {
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            Context.getPermissionsManager().refreshAllExtendedPermissions();
        }
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @PUT
    public Response update(T entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        if (baseClass.equals(Device.class)) {
            Context.getPermissionsManager().checkDeviceReadonly(getUserId());
        } else if (baseClass.equals(User.class)) {
            User before = Context.getPermissionsManager().getUser(entity.getId());
            Context.getPermissionsManager().checkUserUpdate(getUserId(), before, (User) entity);
        } else if (baseClass.equals(Command.class)) {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        } else if (entity instanceof GroupedModel && ((GroupedModel) entity).getGroupId() != 0) {
            Context.getPermissionsManager().checkPermission(
                    Group.class, getUserId(), ((GroupedModel) entity).getGroupId());
        } else if (entity instanceof ScheduledModel && ((ScheduledModel) entity).getCalendarId() != 0) {
            Context.getPermissionsManager().checkPermission(
                    Calendar.class, getUserId(), ((ScheduledModel) entity).getCalendarId());
        }
        Context.getPermissionsManager().checkPermission(baseClass, getUserId(), entity.getId());

        Context.getManager(baseClass).updateItem(entity);
        LogAction.edit(getUserId(), entity);

        if (baseClass.equals(Group.class) || baseClass.equals(Device.class)) {
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            Context.getPermissionsManager().refreshAllExtendedPermissions();
        }
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        if (baseClass.equals(Device.class)) {
            Context.getPermissionsManager().checkDeviceReadonly(getUserId());
        } else if (baseClass.equals(Command.class)) {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        }
        Context.getPermissionsManager().checkPermission(baseClass, getUserId(), id);

        BaseObjectManager<T> manager = Context.getManager(baseClass);
        manager.removeItem(id);
        LogAction.remove(getUserId(), baseClass, id);

        if (manager instanceof SimpleObjectManager) {
            ((SimpleObjectManager<T>) manager).refreshUserItems();
            if (manager instanceof ExtendedObjectManager) {
                ((ExtendedObjectManager<T>) manager).refreshExtendedPermissions();
            }
        }
        if (baseClass.equals(Group.class) || baseClass.equals(Device.class) || baseClass.equals(User.class)) {
            if (baseClass.equals(Group.class)) {
                Context.getGroupsManager().updateGroupCache(true);
                Context.getDeviceManager().updateDeviceCache(true);
            }
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            if (baseClass.equals(User.class)) {
                Context.getPermissionsManager().refreshAllUsersPermissions();
            } else {
                Context.getPermissionsManager().refreshAllExtendedPermissions();
            }
        } else if (baseClass.equals(Calendar.class)) {
            Context.getGeofenceManager().refreshItems();
            Context.getNotificationManager().refreshItems();
        }
        return Response.noContent().build();
    }

}
