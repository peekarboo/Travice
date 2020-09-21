
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.travice.BaseProtocolDecoder;
import org.travice.DeviceSession;
import org.travice.NetworkMessage;
import org.travice.helper.DateBuilder;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class PricolProtocolDecoder extends BaseProtocolDecoder {

    public PricolProtocolDecoder(PricolProtocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        buf.readUnsignedByte(); // header

        DeviceSession deviceSession = getDeviceSession(
                channel, remoteAddress, buf.readSlice(7).toString(StandardCharsets.US_ASCII));
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        position.set("eventType", buf.readUnsignedByte());
        position.set("packetVersion", buf.readUnsignedByte());
        position.set(Position.KEY_STATUS, buf.readUnsignedByte());
        position.set(Position.KEY_RSSI, buf.readUnsignedByte());
        position.set(Position.KEY_GPS, buf.readUnsignedByte());

        position.setTime(new DateBuilder()
                .setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).getDate());

        position.setValid(true);

        double lat = buf.getUnsignedShort(buf.readerIndex()) / 100;
        lat += (buf.readUnsignedShort() % 100 * 10000 + buf.readUnsignedShort()) / 600000.0;
        position.setLatitude(buf.readUnsignedByte() == 'S' ? -lat : lat);

        double lon = buf.getUnsignedMedium(buf.readerIndex()) / 100;
        lon += (buf.readUnsignedMedium() % 100 * 10000 + buf.readUnsignedShort()) / 600000.0;
        position.setLongitude(buf.readUnsignedByte() == 'W' ? -lon : lon);

        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));

        position.set(Position.KEY_INPUT, buf.readUnsignedShort());
        position.set(Position.KEY_OUTPUT, buf.readUnsignedByte());

        position.set("analogAlerts", buf.readUnsignedByte());
        position.set("customAlertTypes", buf.readUnsignedShort());

        for (int i = 1; i <= 5; i++) {
            position.set(Position.PREFIX_ADC + i, buf.readUnsignedShort());
        }

        position.set(Position.KEY_ODOMETER, buf.readUnsignedMedium());
        position.set(Position.KEY_RPM, buf.readUnsignedShort());

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage(
                    Unpooled.copiedBuffer("ACK", StandardCharsets.US_ASCII), remoteAddress));
        }

        return position;
    }

}
