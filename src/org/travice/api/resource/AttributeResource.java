
package org.travice.api.resource;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.travice.Context;
import org.travice.api.ExtendedObjectResource;
import org.travice.model.Attribute;
import org.travice.model.Position;
import org.travice.processing.ComputedAttributesHandler;

@Path("attributes/computed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AttributeResource extends ExtendedObjectResource<Attribute> {

    public AttributeResource() {
        super(Attribute.class);
    }

    @POST
    @Path("test")
    public Response test(@QueryParam("deviceId") long deviceId, Attribute entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        Position last = Context.getIdentityManager().getLastPosition(deviceId);
        if (last != null) {
            Object result = new ComputedAttributesHandler().computeAttribute(entity, last);
            if (result != null) {
                switch (entity.getType()) {
                    case "number":
                        Number numberValue = (Number) result;
                        return Response.ok(numberValue).build();
                    case "boolean":
                        Boolean booleanValue = (Boolean) result;
                        return Response.ok(booleanValue).build();
                    default:
                        return Response.ok(result.toString()).build();
                }
            } else {
                return Response.noContent().build();
            }
        } else {
            throw new IllegalArgumentException("Device has no last position");
        }
    }

}
