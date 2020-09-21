
package org.travice.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.travice.BaseProtocolEncoder;
import org.travice.helper.DataConverter;
import org.travice.helper.Log;
import org.travice.model.Command;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HuabaoProtocolEncoder extends BaseProtocolEncoder {

    @Override
    protected Object encodeCommand(Command command) {

        ByteBuf id = Unpooled.wrappedBuffer(
                DataConverter.parseHex(getUniqueId(command.getDeviceId())));
        try {
            ByteBuf data = Unpooled.buffer();
            byte[] time = DataConverter.parseHex(new SimpleDateFormat("yyMMddHHmmss").format(new Date()));

            switch (command.getType()) {
                case Command.TYPE_ENGINE_STOP:
                    data.writeByte(0x01);
                    data.writeBytes(time);
                    return HuabaoProtocolDecoder.formatMessage(HuabaoProtocolDecoder.MSG_OIL_CONTROL, id, data);
                case Command.TYPE_ENGINE_RESUME:
                    data.writeByte(0x00);
                    data.writeBytes(time);
                    return HuabaoProtocolDecoder.formatMessage(HuabaoProtocolDecoder.MSG_OIL_CONTROL, id, data);
                default:
                    Log.warning(new UnsupportedOperationException(command.getType()));
                    return null;
            }
        } finally {
            id.release();
        }
    }

}
