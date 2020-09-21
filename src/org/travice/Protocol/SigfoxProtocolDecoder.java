
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.travice.BaseHttpProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.helper.DataConverter;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class SigfoxProtocolDecoder extends BaseHttpProtocolDecoder {

    public SigfoxProtocolDecoder(SigfoxProtocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        JsonObject json = Json.createReader(new StringReader(URLDecoder.decode(
                request.content().toString(StandardCharsets.UTF_8).split("=")[0], "UTF-8"))).readObject();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, json.getString("device"));
        if (deviceSession == null) {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.setTime(new Date(json.getInt("time") * 1000L));

        ByteBuf buf = Unpooled.wrappedBuffer(DataConverter.parseHex(json.getString("data")));
        try {
            int type = buf.readUnsignedByte() >> 4;
            if (type == 0) {

                position.setValid(true);
                position.setLatitude(buf.readIntLE() * 0.0000001);
                position.setLongitude(buf.readIntLE() * 0.0000001);
                position.setCourse(buf.readUnsignedByte() * 2);
                position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));

                position.set(Position.KEY_BATTERY, buf.readUnsignedByte() * 0.025);

            } else {

                getLastLocation(position, position.getDeviceTime());

            }
        } finally {
            buf.release();
        }

        position.set(Position.KEY_RSSI, json.getJsonNumber("rssi").doubleValue());
        position.set(Position.KEY_INDEX, json.getInt("seqNumber"));

        sendResponse(channel, HttpResponseStatus.OK);
        return position;
    }

}
