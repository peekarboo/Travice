
package org.travice.api.resource;

import org.travice.api.SimpleObjectResource;
import org.travice.model.Group;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupResource extends SimpleObjectResource<Group> {

    public GroupResource() {
        super(Group.class);
    }

}
