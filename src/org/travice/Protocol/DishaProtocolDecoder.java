
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class DishaProtocolDecoder extends BaseProtocolDecoder {

    public DishaProtocolDecoder(DishaProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$A#A#")
            .number("(d+)#")                     // imei
            .expression("([AVMX])#")             // validity
            .number("(dd)(dd)(dd)#")             // time (hhmmss)
            .number("(dd)(dd)(dd)#")             // date (ddmmyy)
            .number("(dd)(dd.d+)#")              // latitude
            .expression("([NS])#")
            .number("(ddd)(dd.d+)#")             // longitude
            .expression("([EW])#")
            .number("(d+.d+)#")                  // speed
            .number("(d+.d+)#")                  // course
            .number("(d+)#")                     // satellites
            .number("(d+.d+)#")                  // hdop
            .number("(d+)#")                     // gsm
            .expression("([012])#")              // power mode
            .number("(d+)#")                     // battery
            .number("(d+)#")                     // adc 1
            .number("(d+)#")                     // adc 2
            .number("d+.d+#")                    // day distance
            .number("(d+.d+)#")                  // odometer
            .expression("([01]+)")               // digital inputs
            .text("*")
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.setValid(parser.next().equals("A"));

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));

        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());

        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        position.set(Position.KEY_HDOP, parser.nextDouble());
        position.set(Position.KEY_RSSI, parser.nextDouble());
        position.set(Position.KEY_CHARGE, parser.nextInt(0) == 2);
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt(0));

        position.set(Position.PREFIX_ADC + 1, parser.nextInt(0));
        position.set(Position.PREFIX_ADC + 2, parser.nextInt(0));

        position.set(Position.KEY_ODOMETER, parser.nextDouble(0) * 1000);
        position.set(Position.KEY_INPUT, parser.next());

        return position;
    }

}
