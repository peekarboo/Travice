
package org.travice.protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.Protocol;
import org.travice.helper.Parser;
import org.travice.helper.Parser.CoordinateFormat;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

public class IdplProtocolDecoder extends BaseProtocolDecoder {

    public IdplProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("*ID")                         // start of frame
            .number("(d+),")                     // command code
            .number("(d+),")                     // imei
            .number("(dd)(dd)(dd),")             // current date (ddmmyy)
            .number("(dd)(dd)(dd),")             // current time (hhmmss)
            .expression("([A|V]),")              // gps fix
            .number("(dd)(dd).?(d+),([NS]),")    // latitude
            .number("(ddd)(dd).?(d+),([EW]),")   // longitude
            .number("(d{1,3}.dd),")              // speed
            .number("(d{1,3}.dd),")              // course
            .number("(d{1,2}),")                 // sats
            .number("(d{1,3}),")                 // gsm signal strength
            .expression("([A|N|S]),")            // vehicle status
            .expression("([0|1]),")              // main power status
            .number("(d.dd),")                   // internal battery voltage
            .expression("([0|1]),")              // sos alert
            .expression("([0|1]),")              // body tamper
            .expression("([0|1])([0|1]),")       // ac status + ign status
            .expression("([0|1|2]),")            // output1 status
            .number("(d{1,3}),")                 // adc1
            .number("(d{1,3}),")                 // adc2
            .expression("([0-9A-Z]{3}),")        // software version
            .expression("([L|R]),")              // message type
            .number("(x{4})#")                   // crc
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        position.set(Position.KEY_TYPE, parser.nextInt(0));

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate(CoordinateFormat.DEG_MIN_MIN_HEM));
        position.setLongitude(parser.nextCoordinate(CoordinateFormat.DEG_MIN_MIN_HEM));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_SATELLITES, parser.nextInt(0));
        position.set(Position.KEY_RSSI, parser.nextInt(0));
        position.set("vehicleStatus", parser.next());
        position.set(Position.KEY_POWER, parser.nextInt(0));
        position.set(Position.KEY_BATTERY, parser.nextDouble(0));
        if (parser.nextInt(0) == 1) {
            position.set(Position.KEY_ALARM, Position.ALARM_SOS);
        }
        parser.nextInt(0); // body tamper
        position.set("acStatus", parser.nextInt(0));
        position.set(Position.KEY_IGNITION, parser.nextInt(0) == 1);
        position.set(Position.KEY_OUTPUT, parser.nextInt(0));
        position.set(Position.PREFIX_ADC + 1, parser.nextInt(0));
        position.set(Position.PREFIX_ADC + 2, parser.nextInt(0));
        position.set(Position.KEY_VERSION_FW, parser.next());
        position.set(Position.KEY_ARCHIVE, parser.next().equals("R"));

        parser.next(); // checksum

        return position;
    }

}
