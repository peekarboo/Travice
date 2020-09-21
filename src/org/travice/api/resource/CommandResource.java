
package org.travice.api.resource;

import org.travice.Context;
import org.travice.api.ExtendedObjectResource;
import org.travice.database.CommandsManager;
import org.travice.model.Command;
import org.travice.model.Typed;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandResource extends ExtendedObjectResource<Command> {

    public CommandResource() {
        super(Command.class);
    }

    @GET
    @Path("send")
    public Collection<Command> get(@QueryParam("deviceId") long deviceId) throws SQLException {
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        CommandsManager commandsManager = Context.getCommandsManager();
        Set<Long> result = new HashSet<>(commandsManager.getUserItems(getUserId()));
        result.retainAll(commandsManager.getSupportedCommands(deviceId));
        return commandsManager.getItems(result);
    }

    @POST
    @Path("send")
    public Response send(Command entity) throws Exception {
        Context.getPermissionsManager().checkReadonly(getUserId());
        long deviceId = entity.getDeviceId();
        long id = entity.getId();
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        if (id != 0) {
            Context.getPermissionsManager().checkPermission(Command.class, getUserId(), id);
            Context.getPermissionsManager().checkUserDeviceCommand(getUserId(), deviceId, id);
        } else {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        }
        if (!Context.getCommandsManager().sendCommand(entity)) {
            return Response.accepted(entity).build();
        }
        return Response.ok(entity).build();
    }

    @GET
    @Path("types")
    public Collection<Typed> get(@QueryParam("deviceId") long deviceId,
            @QueryParam("textChannel") boolean textChannel) {
        if (deviceId != 0) {
            Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
            return Context.getCommandsManager().getCommandTypes(deviceId, textChannel);
        } else {
            return Context.getCommandsManager().getAllCommandTypes();
        }
    }
}
