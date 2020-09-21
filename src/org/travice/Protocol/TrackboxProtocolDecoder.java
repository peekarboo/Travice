
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.DateBuilder;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class TrackboxProtocolDecoder extends BaseProtocolDecoder {

    public TrackboxProtocolDecoder(TrackboxProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(dd)(dd)(dd).(ddd),")       // time (hhmmss.sss)
            .number("(dd)(dd.dddd)([NS]),")      // latitude
            .number("(ddd)(dd.dddd)([EW]),")     // longitude
            .number("(d+.d),")                   // hdop
            .number("(-?d+.?d*),")               // altitude
            .number("(d),")                      // fix type
            .number("(d+.d+),")                  // course
            .number("d+.d+,")                    // speed (kph)
            .number("(d+.d+),")                  // speed (knots)
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(d+)")                      // satellites
            .compile();

    private void sendResponse(Channel channel, SocketAddress remoteAddress) {
        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage("=OK=\r\n", remoteAddress));
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        if (sentence.startsWith("a=connect")) {
            String id = sentence.substring(sentence.indexOf("i=") + 2);
            if (getDeviceSession(channel, remoteAddress, id) != null) {
                sendResponse(channel, remoteAddress);
            }
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) {
            return null;
        }

        Parser parser = new Parser(PATTERN, sentence);
        if (!parser.matches()) {
            return null;
        }
        sendResponse(channel, remoteAddress);

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());

        position.set(Position.KEY_HDOP, parser.nextDouble());

        position.setAltitude(parser.nextDouble(0));

        int fix = parser.nextInt(0);
        position.set(Position.KEY_GPS, fix);
        position.setValid(fix > 0);

        position.setCourse(parser.nextDouble(0));
        position.setSpeed(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
        position.setTime(dateBuilder.getDate());

        position.set(Position.KEY_SATELLITES, parser.nextInt());

        return position;
    }

}
