
package org.travice.protocol;

import java.util.TimeZone;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.Checksum;
import org.travice.helper.Log;
import org.travice.model.Command;

public class CityeasyProtocolEncoder extends BaseProtocolEncoder {

    private ByteBuf encodeContent(int type, ByteBuf content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeByte('S');
        buf.writeByte('S');
        buf.writeShort(2 + 2 + 2 + content.readableBytes() + 4 + 2 + 2);
        buf.writeShort(type);
        buf.writeBytes(content);
        buf.writeInt(0x0B);
        buf.writeShort(Checksum.crc16(Checksum.CRC16_KERMIT, buf.nioBuffer()));
        buf.writeByte('\r');
        buf.writeByte('\n');

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        ByteBuf content = Unpooled.buffer();

        switch (command.getType()) {
            case Command.TYPE_POSITION_SINGLE:
                return encodeContent(CityeasyProtocolDecoder.MSG_LOCATION_REQUEST, content);
            case Command.TYPE_POSITION_PERIODIC:
                content.writeShort(command.getInteger(Command.KEY_FREQUENCY));
                return encodeContent(CityeasyProtocolDecoder.MSG_LOCATION_INTERVAL, content);
            case Command.TYPE_POSITION_STOP:
                content.writeShort(0);
                return encodeContent(CityeasyProtocolDecoder.MSG_LOCATION_INTERVAL, content);
            case Command.TYPE_SET_TIMEZONE:
                int timezone = TimeZone.getTimeZone(command.getString(Command.KEY_TIMEZONE)).getRawOffset() / 60000;
                if (timezone < 0) {
                    content.writeByte(1);
                } else {
                    content.writeByte(0);
                }
                content.writeShort(Math.abs(timezone));
                return encodeContent(CityeasyProtocolDecoder.MSG_TIMEZONE, content);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
