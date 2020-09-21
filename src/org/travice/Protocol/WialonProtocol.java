
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.Context;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class WialonProtocol extends BaseProtocol {

    public WialonProtocol() {
        super("wialon");
        setSupportedDataCommands(
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_SEND_USSD,
                Command.TYPE_IDENTIFICATION,
                Command.TYPE_OUTPUT_CONTROL);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(4 * 1024));
                pipeline.addLast("stringEncoder", new StringEncoder());
                boolean utf8 = Context.getConfig().getBoolean(getName() + ".utf8");
                if (utf8) {
                    pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
                } else {
                    pipeline.addLast("stringDecoder", new StringDecoder());
                }
                pipeline.addLast("objectEncoder", new WialonProtocolEncoder());
                pipeline.addLast("objectDecoder", new WialonProtocolDecoder(WialonProtocol.this));
            }
        });
    }

}
