
package org.travice.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.travice.api.ExtendedObjectResource;
import org.travice.model.Driver;

@Path("drivers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DriverResource extends ExtendedObjectResource<Driver> {

    public DriverResource() {
        super(Driver.class);
    }

}
