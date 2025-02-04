
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.Checksum;
import org.travice.helper.Log;
import org.travice.model.Command;

import java.nio.charset.StandardCharsets;

public class RuptelaProtocolEncoder extends BaseProtocolEncoder {

    private ByteBuf encodeContent(int type, ByteBuf content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeShort(1 + content.readableBytes());
        buf.writeByte(100 + type);
        buf.writeBytes(content);
        buf.writeShort(Checksum.crc16(Checksum.CRC16_KERMIT, buf.nioBuffer(2, buf.writerIndex() - 2)));

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        ByteBuf content = Unpooled.buffer();

        switch (command.getType()) {
            case Command.TYPE_CUSTOM:
                content.writeBytes(command.getString(Command.KEY_DATA).getBytes(StandardCharsets.US_ASCII));
                return encodeContent(RuptelaProtocolDecoder.MSG_SMS_VIA_GPRS, content);
            case Command.TYPE_CONFIGURATION:
                content.writeBytes((command.getString(Command.KEY_DATA) + "\r\n").getBytes(StandardCharsets.US_ASCII));
                return encodeContent(RuptelaProtocolDecoder.MSG_DEVICE_CONFIGURATION, content);
            case Command.TYPE_GET_VERSION:
                return encodeContent(RuptelaProtocolDecoder.MSG_DEVICE_VERSION, content);
            case Command.TYPE_FIRMWARE_UPDATE:
                content.writeBytes("|FU_STRT*\r\n".getBytes(StandardCharsets.US_ASCII));
                return encodeContent(RuptelaProtocolDecoder.MSG_FIRMWARE_UPDATE, content);
            case Command.TYPE_OUTPUT_CONTROL:
                content.writeInt(command.getInteger(Command.KEY_INDEX));
                content.writeInt(Integer.parseInt(command.getString(Command.KEY_DATA)));
                return encodeContent(RuptelaProtocolDecoder.MSG_SET_IO, content);
            case Command.TYPE_SET_CONNECTION:
                String c = command.getString(Command.KEY_SERVER) + "," + command.getInteger(Command.KEY_PORT) + ",TCP";
                content.writeBytes(c.getBytes(StandardCharsets.US_ASCII));
                return encodeContent(RuptelaProtocolDecoder.MSG_SET_CONNECTION, content);
            case Command.TYPE_SET_ODOMETER:
                content.writeInt(Integer.parseInt(command.getString(Command.KEY_DATA)));
                return encodeContent(RuptelaProtocolDecoder.MSG_SET_ODOMETER, content);
            default:
                Log.warning(new UnsupportedOperationException(command.getType()));
                break;
        }

        return null;
    }

}
