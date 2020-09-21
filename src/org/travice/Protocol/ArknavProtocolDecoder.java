
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class ArknavProtocolDecoder extends BaseProtocolDecoder {

    public ArknavProtocolDecoder(ArknavProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .number("(d+),")                     // imei
            .expression(".{6},")                 // id code
            .number("ddd,")                      // status
            .number("Lddd,")                     // version
            .expression("([AV]),")               // validity
            .number("(dd)(dd.d+),")              // latitude
            .expression("([NS]),")
            .number("(ddd)(dd.d+),")             // longitude
            .expression("([EW]),")
            .number("(d+.?d*),")                 // speed
            .number("(d+.?d*),")                 // course
            .number("(d+.?d*),")                 // hdop
            .number("(dd):(dd):(dd) ")           // time (hh:mm:ss)
            .number("(dd)-(dd)-(dd),")           // date (dd-mm-yy)
            .any()
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
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_HDOP, parser.nextDouble(0));

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));

        return position;
    }

}
