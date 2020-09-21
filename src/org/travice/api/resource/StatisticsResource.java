
package org.travice.api.resource;

import org.travice.Context;
import org.travice.api.BaseResource;
import org.travice.helper.DateUtil;
import org.travice.model.Statistics;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.Collection;

@Path("statistics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatisticsResource extends BaseResource {

    @GET
    public Collection<Statistics> get(
            @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return Context.getDataManager().getStatistics(DateUtil.parseDate(from), DateUtil.parseDate(to));
    }

}
