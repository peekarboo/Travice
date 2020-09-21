
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TlvProtocolDecoder extends BaseProtocolDecoder {

    public TlvProtocolDecoder(TlvProtocol protocol) {
        super(protocol);
    }

    private void sendResponse(Channel channel, SocketAddress remoteAddress, String type, String... arguments) {
        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
            response.writeCharSequence(type, StandardCharsets.US_ASCII);
            for (String argument : arguments) {
                response.writeByte(argument.length());
                response.writeCharSequence(argument, StandardCharsets.US_ASCII);
            }
            response.writeByte(0);
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }
    }

    private String readArgument(ByteBuf buf) {
        return buf.readSlice(buf.readUnsignedByte()).toString(StandardCharsets.US_ASCII);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        String type = buf.readSlice(2).toString(StandardCharsets.US_ASCII);

        if (channel != null) {
            switch (type) {
                case "0A":
                case "0C":
                    sendResponse(channel, remoteAddress, type);
                    break;
                case "0B":
                    sendResponse(channel, remoteAddress, type, "1482202689", "10", "20", "15");
                    break;
                case "0E":
                case "0F":
                    sendResponse(channel, remoteAddress, type, "30", "Unknown");
                    break;
                default:
                    break;
            }
        }

        if (type.equals("0E")) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, readArgument(buf));
            if (deviceSession == null) {
                return null;
            }

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.setValid(true);
            position.setTime(new Date(Long.parseLong(readArgument(buf)) * 1000));

            readArgument(buf); // location identifier

            position.setLongitude(Double.parseDouble(readArgument(buf)));
            position.setLatitude(Double.parseDouble(readArgument(buf)));
            position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(readArgument(buf))));
            position.setCourse(Double.parseDouble(readArgument(buf)));

            position.set(Position.KEY_SATELLITES, Integer.parseInt(readArgument(buf)));

            return position;

        }

        return null;
    }

}
