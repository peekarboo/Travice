
package org.travice.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.Parser;
import org.travice.helper.PatternBuilder;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class PathAwayProtocolDecoder extends BaseProtocolDecoder {

    public PathAwayProtocolDecoder(PathAwayProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$PWS,")
            .number("d+,")                       // version
            .expression("[^,]*,")                // name
            .expression("[^,]*,")                // icon
            .expression("[^,]*,")                // color
            .number("(dd)(dd)(dd),")             // date (ddmmyy)
            .number("(dd)(dd)(dd),")             // time (hhmmss)
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(-?d+.?d*),")               // altitude
            .number("(-?d+.?d*),")               // speed
            .number("(-?d+.?d*),")               // course
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        DeviceSession deviceSession = getDeviceSession(
                channel, remoteAddress, decoder.parameters().get("UserName").get(0));
        if (deviceSession == null) {
            return null;
        }

        Parser parser = new Parser(PATTERN, decoder.parameters().get("LOC").get(0));
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setValid(true);
        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        if (channel != null) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress)).addListener(ChannelFutureListener.CLOSE);
        }

        return position;
    }

}
