
package org.travice;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.travice.helper.Checksum;
import org.travice.helper.Log;
import org.travice.model.Device;
import org.travice.model.Position;

import javax.ws.rs.client.Entity;
import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class WebDataHandler extends BaseDataHandler {

    private final String url;
    private final boolean json;
    private static final String KEY_POSITION = "position";
    private static final String KEY_DEVICE = "device";

    public WebDataHandler(String url, boolean json) {
        this.url = url;
        this.json = json;
    }

    private static String formatSentence(Position position) {

        StringBuilder s = new StringBuilder("$GPRMC,");

        try (Formatter f = new Formatter(s, Locale.ENGLISH)) {

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
            calendar.setTimeInMillis(position.getFixTime().getTime());

            f.format("%1$tH%1$tM%1$tS.%1$tL,A,", calendar);

            double lat = position.getLatitude();
            double lon = position.getLongitude();

            f.format("%02d%07.4f,%c,", (int) Math.abs(lat), Math.abs(lat) % 1 * 60, lat < 0 ? 'S' : 'N');
            f.format("%03d%07.4f,%c,", (int) Math.abs(lon), Math.abs(lon) % 1 * 60, lon < 0 ? 'W' : 'E');

            f.format("%.2f,%.2f,", position.getSpeed(), position.getCourse());
            f.format("%1$td%1$tm%1$ty,,", calendar);
        }

        s.append(Checksum.nmea(s.toString()));

        return s.toString();
    }

    private String calculateStatus(Position position) {
        if (position.getAttributes().containsKey(Position.KEY_ALARM)) {
            return "0xF841"; // STATUS_PANIC_ON
        } else if (position.getSpeed() < 1.0) {
            return "0xF020"; // STATUS_LOCATION
        } else {
            return "0xF11C"; // STATUS_MOTION_MOVING
        }
    }

    public String formatRequest(Position position) {

        Device device = Context.getIdentityManager().getById(position.getDeviceId());

        String request = url
                .replace("{name}", device.getName())
                .replace("{uniqueId}", device.getUniqueId())
                .replace("{status}", device.getStatus())
                .replace("{deviceId}", String.valueOf(position.getDeviceId()))
                .replace("{protocol}", String.valueOf(position.getProtocol()))
                .replace("{deviceTime}", String.valueOf(position.getDeviceTime().getTime()))
                .replace("{fixTime}", String.valueOf(position.getFixTime().getTime()))
                .replace("{valid}", String.valueOf(position.getValid()))
                .replace("{latitude}", String.valueOf(position.getLatitude()))
                .replace("{longitude}", String.valueOf(position.getLongitude()))
                .replace("{altitude}", String.valueOf(position.getAltitude()))
                .replace("{speed}", String.valueOf(position.getSpeed()))
                .replace("{course}", String.valueOf(position.getCourse()))
                .replace("{accuracy}", String.valueOf(position.getAccuracy()))
                .replace("{statusCode}", calculateStatus(position));

        if (position.getAddress() != null) {
            try {
                request = request.replace(
                        "{address}", URLEncoder.encode(position.getAddress(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException error) {
                Log.warning(error);
            }
        }

        if (request.contains("{attributes}")) {
            try {
                String attributes = Context.getObjectMapper().writeValueAsString(position.getAttributes());
                request = request.replace(
                        "{attributes}", URLEncoder.encode(attributes, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException | JsonProcessingException error) {
                Log.warning(error);
            }
        }

        if (request.contains("{gprmc}")) {
            request = request.replace("{gprmc}", formatSentence(position));
        }

        return request;
    }

    @Override
    protected Position handlePosition(Position position) {
        if (json) {
            Context.getClient().target(url).request().async().post(Entity.json(prepareJsonPayload(position)));
        } else {
            Context.getClient().target(formatRequest(position)).request().async().get();
        }
        return position;
    }

    protected Map<String, Object> prepareJsonPayload(Position position) {

        Map<String, Object> data = new HashMap<>();
        Device device = Context.getIdentityManager().getById(position.getDeviceId());

        data.put(KEY_POSITION, position);

        if (device != null) {
            data.put(KEY_DEVICE, device);
        }

        return data;
    }

}
