
package org.travice.protocol;

import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.CellTower;
import org.travice.model.Network;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class Tt8850ProtocolDecoder extends BaseProtocolDecoder {

    public Tt8850ProtocolDecoder(Tt8850Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .binary("0004,")
            .number("xxxx,")
            .expression("[01],")
            .expression("GT...,")
            .number("(?:[0-9A-Z]{2}xxxx)?,")     // protocol version
            .expression("([^,]+),")              // imei
            .any()
            .number("(d{1,2})?,")                // gps accuracy
            .number("(d{1,3}.d)?,")              // speed
            .number("(d{1,3})?,")                // course
            .number("(-?d{1,5}.d)?,")            // altitude
            .number("(-?d{1,3}.d{6}),")          // longitude
            .number("(-?d{1,2}.d{6}),")          // latitude
            .number("(dddd)(dd)(dd)")            // date (yyyymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(0ddd)?,")                  // mcc
            .number("(0ddd)?,")                  // mnc
            .number("(xxxx)?,")                  // lac
            .number("(xxxx)?,")                  // cell
            .any()
            .number("(dddd)(dd)(dd)")            // date (yyyymmdd)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(xxxx)")
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

        position.setValid(true);
        position.setAccuracy(parser.nextInt(0));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setLatitude(parser.nextDouble(0));

        position.setTime(parser.nextDateTime());

        if (parser.hasNext(4)) {
            position.setNetwork(new Network(
                    CellTower.from(parser.nextInt(0), parser.nextInt(0), parser.nextHexInt(0), parser.nextHexInt(0))));
        }

        return position;
    }

}
