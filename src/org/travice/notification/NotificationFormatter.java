
package org.travice.notification;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.travice.Context;
import org.travice.helper.Log;
import org.travice.model.Device;
import org.travice.model.Event;
import org.travice.model.Position;
import org.travice.model.User;
import org.travice.reports.ReportUtils;

public final class NotificationFormatter {

    private NotificationFormatter() {
    }

    public static VelocityContext prepareContext(long userId, Event event, Position position) {

        User user = Context.getPermissionsManager().getUser(userId);
        Device device = Context.getIdentityManager().getById(event.getDeviceId());

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("user", user);
        velocityContext.put("device", device);
        velocityContext.put("event", event);
        if (position != null) {
            velocityContext.put("position", position);
            velocityContext.put("speedUnit", ReportUtils.getSpeedUnit(userId));
            velocityContext.put("distanceUnit", ReportUtils.getDistanceUnit(userId));
            velocityContext.put("volumeUnit", ReportUtils.getVolumeUnit(userId));
        }
        if (event.getGeofenceId() != 0) {
            velocityContext.put("geofence", Context.getGeofenceManager().getById(event.getGeofenceId()));
        }
        if (event.getMaintenanceId() != 0) {
            velocityContext.put("maintenance", Context.getMaintenancesManager().getById(event.getMaintenanceId()));
        }
        String driverUniqueId = event.getString(Position.KEY_DRIVER_UNIQUE_ID);
        if (driverUniqueId != null) {
            velocityContext.put("driver", Context.getDriversManager().getDriverByUniqueId(driverUniqueId));
        }
        velocityContext.put("webUrl", Context.getVelocityEngine().getProperty("web.url"));
        velocityContext.put("dateTool", new DateTool());
        velocityContext.put("numberTool", new NumberTool());
        velocityContext.put("timezone", ReportUtils.getTimezone(userId));
        velocityContext.put("locale", Locale.getDefault());
        return velocityContext;
    }

    public static Template getTemplate(Event event, String path) {

        String templateFilePath;
        Template template;

        try {
            templateFilePath = Paths.get(path, event.getType() + ".vm").toString();
            template = Context.getVelocityEngine().getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
        } catch (ResourceNotFoundException error) {
            Log.warning(error);
            templateFilePath = Paths.get(path, "unknown.vm").toString();
            template = Context.getVelocityEngine().getTemplate(templateFilePath, StandardCharsets.UTF_8.name());
        }
        return template;
    }

    public static FullMessage formatFullMessage(long userId, Event event, Position position) {
        VelocityContext velocityContext = prepareContext(userId, event, position);
        String formattedMessage = formatMessage(velocityContext, userId, event, position, "full");

        return new FullMessage((String) velocityContext.get("subject"), formattedMessage);
    }

    public static String formatShortMessage(long userId, Event event, Position position) {
        return formatMessage(null, userId, event, position, "short");
    }

    private static String formatMessage(VelocityContext vc, Long userId, Event event, Position position,
            String templatePath) {

        VelocityContext velocityContext = vc;
        if (velocityContext == null) {
            velocityContext = prepareContext(userId, event, position);
        }
        StringWriter writer = new StringWriter();
        getTemplate(event, templatePath).merge(velocityContext, writer);

        return writer.toString();
    }

}
