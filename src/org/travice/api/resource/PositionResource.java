
package org.travice.api.resource;

import org.travice.Context;
import org.travice.api.BaseResource;
import org.travice.helper.DateUtil;
import org.travice.model.Position;
import org.travice.web.CsvBuilder;
import org.travice.web.GpxBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PositionResource extends BaseResource {

    public static final String TEXT_CSV = "text/csv";
    public static final String CONTENT_DISPOSITION_VALUE_CSV = "attachment; filename=positions.csv";
    public static final String GPX = "application/gpx+xml";
    public static final String CONTENT_DISPOSITION_VALUE_GPX = "attachment; filename=positions.gpx";

    @GET
    public Collection<Position> getJson(
            @QueryParam("deviceId") long deviceId, @QueryParam("id") List<Long> positionIds,
            @QueryParam("from") String from, @QueryParam("to") String to)
            throws SQLException {
        if (!positionIds.isEmpty()) {
            ArrayList<Position> positions = new ArrayList<>();
            for (Long positionId : positionIds) {
                Position position = Context.getDataManager().getObject(Position.class, positionId);
                Context.getPermissionsManager().checkDevice(getUserId(), position.getDeviceId());
                positions.add(position);
            }
            return positions;
        } else if (deviceId == 0) {
            return Context.getDeviceManager().getInitialState(getUserId());
        } else {
            Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
            return Context.getDataManager().getPositions(
                    deviceId, DateUtil.parseDate(from), DateUtil.parseDate(to));
        }
    }

    @GET
    @Produces(TEXT_CSV)
    public Response getCsv(
            @QueryParam("deviceId") long deviceId, @QueryParam("from") String from, @QueryParam("to") String to)
            throws SQLException {
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        CsvBuilder csv = new CsvBuilder();
        csv.addHeaderLine(new Position());
        csv.addArray(Context.getDataManager().getPositions(
                deviceId, DateUtil.parseDate(from), DateUtil.parseDate(to)));
        return Response.ok(csv.build()).header(HttpHeaders.CONTENT_DISPOSITION, CONTENT_DISPOSITION_VALUE_CSV).build();
    }

    @GET
    @Produces(GPX)
    public Response getGpx(
            @QueryParam("deviceId") long deviceId, @QueryParam("from") String from, @QueryParam("to") String to)
            throws SQLException {
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        GpxBuilder gpx = new GpxBuilder(Context.getIdentityManager().getById(deviceId).getName());
        gpx.addPositions(Context.getDataManager().getPositions(
                deviceId, DateUtil.parseDate(from), DateUtil.parseDate(to)));
        return Response.ok(gpx.build()).header(HttpHeaders.CONTENT_DISPOSITION, CONTENT_DISPOSITION_VALUE_GPX).build();
    }

}
