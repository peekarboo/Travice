
package org.travice.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.travice.BaseHttpProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class OpenGtsProtocolDecoder extends BaseHttpProtocolDecoder {

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$GPRMC,")
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("([AV]),")               // validity
            .number("(d+)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(d+)(dd.d+),")              // longitude
            .expression("([EW]),")
            .number("(d+.d+),")                  // speed
            .number("(d+.d+),")                  // course
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .any()
            .compile();

    public OpenGtsProtocolDecoder(OpenGtsProtocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();

        Position position = new Position(getProtocolName());

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String value = entry.getValue().get(0);
            switch (entry.getKey()) {
                case "id":
                    DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, value);
                    if (deviceSession == null) {
                        sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
                        return null;
                    }
                    position.setDeviceId(deviceSession.getDeviceId());
                    break;
                case "gprmc":
                    Parser parser = new Parser(PATTERN, value);
                    if (!parser.matches()) {
                            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
                        return null;
                    }

                    DateBuilder dateBuilder = new DateBuilder()
                            .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

                    position.setValid(parser.next().equals("A"));
                    position.setLatitude(parser.nextCoordinate());
                    position.setLongitude(parser.nextCoordinate());
                    position.setSpeed(parser.nextDouble(0));
                    position.setCourse(parser.nextDouble(0));

                    dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
                    position.setTime(dateBuilder.getDate());
                    break;
                case "alt":
                    position.setAltitude(Double.parseDouble(value));
                    break;
                case "batt":
                    position.set(Position.KEY_BATTERY_LEVEL, Double.parseDouble(value));
                    break;
                default:
                    break;
            }
        }

        if (position.getDeviceId() != 0) {
            sendResponse(channel, HttpResponseStatus.OK);
            return position;
        } else {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
            return null;
        }
    }

}
