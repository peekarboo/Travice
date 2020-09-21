
package org.travice.api.resource;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.travice.Context;
import org.travice.api.ExtendedObjectResource;
import org.travice.model.Event;
import org.travice.model.Notification;
import org.travice.model.Typed;
import org.travice.notification.MessageException;


@Path("notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource extends ExtendedObjectResource<Notification> {

    public NotificationResource() {
        super(Notification.class);
    }

    @GET
    @Path("types")
    public Collection<Typed> get() {
        return Context.getNotificationManager().getAllNotificationTypes();
    }

    @GET
    @Path("notificators")
    public Collection<Typed> getNotificators() {
        return Context.getNotificatorManager().getAllNotificatorTypes();
    }

    @POST
    @Path("test")
    public Response testMessage() throws MessageException, InterruptedException {
        for (Typed method : Context.getNotificatorManager().getAllNotificatorTypes()) {
            Context.getNotificatorManager()
                    .getNotificator(method.getType()).sendSync(getUserId(), new Event("test", 0), null);
        }
        return Response.noContent().build();
    }

    @POST
    @Path("test/{notificator}")
    public Response testMessage(@PathParam("notificator") String notificator)
            throws MessageException, InterruptedException {
        Context.getNotificatorManager().getNotificator(notificator).sendSync(getUserId(), new Event("test", 0), null);
        return Response.noContent().build();
    }

}
