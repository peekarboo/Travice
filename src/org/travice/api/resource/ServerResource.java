package org.travice.api.resource;

import org.travice.Context;
import org.travice.api.BaseResource;
import org.travice.helper.LogAction;
import org.travice.model.Server;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("server")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServerResource extends BaseResource {

    @PermitAll
    @GET
    public Server get() throws SQLException {
        return Context.getPermissionsManager().getServer();
    }

    @PUT
    public Response update(Server entity) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        Context.getPermissionsManager().updateServer(entity);
        LogAction.edit(getUserId(), entity);
        return Response.ok(entity).build();
    }

    @Path("geocode")
    @GET
    public String geocode(@QueryParam("latitude") double latitude, @QueryParam("longitude") double longitude) {
        return Context.getGeocoder().getAddress(latitude, longitude, null);
    }

}
