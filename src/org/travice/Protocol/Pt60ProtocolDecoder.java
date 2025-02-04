
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Pt60ProtocolDecoder extends BaseProtocolDecoder {

    public Pt60ProtocolDecoder(Pt60Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_TRACK = 6;
    public static final int MSG_STEP_COUNT = 13;
    public static final int MSG_HEART_RATE = 14;

    private static final Pattern PATTERN = new PatternBuilder()
            .text("@G#@,")                       // header
            .number("Vdd,")                      // protocol version
            .number("(d+),")                     // type
            .number("(d+),")                     // imei
            .number("(d+),")                     // imsi
            .number("(dddd)(dd)(dd)")            // date (yyyymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .expression("(.*)")                  // data
            .compile();

    private void sendResponse(Channel channel, SocketAddress remoteAddress) {
        if (channel != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            channel.writeAndFlush(new NetworkMessage(
                    "@G#@,V01,38," + dateFormat.format(new Date()) + ",@R#@", remoteAddress));
        }
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        sendResponse(channel, remoteAddress);

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        int type = parser.nextInt();

        if (type != MSG_TRACK && type != MSG_STEP_COUNT && type != MSG_HEART_RATE) {
            return null;
        }

        Position position = new Position(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next(), parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.setDeviceTime(parser.nextDateTime());

        String[] values = parser.next().split(",");

        if (type == MSG_TRACK) {

            position.setValid(true);
            position.setFixTime(position.getDeviceTime());

            String[] coordinates = values[0].split(";");
            position.setLatitude(Double.parseDouble(coordinates[0]));
            position.setLongitude(Double.parseDouble(coordinates[1]));

        } else {

            getLastLocation(position, position.getDeviceTime());

            switch (type) {
                case MSG_STEP_COUNT:
                    position.set(Position.KEY_STEPS, Integer.parseInt(values[0]));
                    break;
                case MSG_HEART_RATE:
                    position.set(Position.KEY_HEART_RATE, Integer.parseInt(values[0]));
                    position.set(Position.KEY_BATTERY, Integer.parseInt(values[1]));
                    break;
                default:
                    break;
            }

        }

        return position;
    }

}
