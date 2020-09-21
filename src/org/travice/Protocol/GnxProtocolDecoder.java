
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class GnxProtocolDecoder extends BaseProtocolDecoder {

    public GnxProtocolDecoder(GnxProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN_LOCATION = new PatternBuilder()
            .number("(d+),")                     // imei
            .number("d+,")                       // length
            .expression("([01]),")               // history
            .number("(dd)(dd)(dd),")             // device time (hhmmss)
            .number("(dd)(dd)(dd),")             // device date (ddmmyy)
            .number("(dd)(dd)(dd),")             // fix time (hhmmss)
            .number("(dd)(dd)(dd),")             // fix date (ddmmyy)
            .number("(d),")                      // valid
            .number("(dd.d+),")                  // latitude
            .expression("([NS]),")
            .number("(ddd.d+),")                 // longitude
            .expression("([EW]),")
            .compile();

    private static final Pattern PATTERN_MIF = new PatternBuilder()
            .text("$GNX_MIF,")
            .expression(PATTERN_LOCATION.pattern())
            .expression("[01],")                 // valid card
            .expression("([^,]+),")              // rfid
            .any()
            .compile();

    private static final Pattern PATTERN_OTHER = new PatternBuilder()
            .text("$GNX_")
            .expression("...,")
            .expression(PATTERN_LOCATION.pattern())
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        String type = sentence.substring(5, 8);

        Pattern pattern;
        if (type.equals("MIF")) {
            pattern = PATTERN_MIF;
        } else {
            pattern = PATTERN_OTHER;
        }

        Parser parser = new Parser(pattern, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        if (parser.nextInt(0) == 1) {
            position.set(Position.KEY_ARCHIVE, true);
        }

        position.setDeviceTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY, "GMT+5:30"));
        position.setFixTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY, "GMT+5:30"));

        position.setValid(parser.nextInt(0) != 0);

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));

        if (type.equals("MIF")) {
            position.set(Position.KEY_DRIVER_UNIQUE_ID, parser.next());
        }

        return position;
    }

}
