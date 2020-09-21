
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import io.netty.handler.codec.string.StringEncoder;

import java.util.List;

public class WondexProtocol extends BaseProtocol {

    public WondexProtocol() {
        super("wondex");
        setTextCommandEncoder(new WondexProtocolEncoder());
        setSupportedCommands(
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_GET_MODEM_STATUS,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_GET_VERSION,
                Command.TYPE_IDENTIFICATION);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new WondexFrameDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new WondexProtocolEncoder());
                pipeline.addLast("objectDecoder", new WondexProtocolDecoder(WondexProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new WondexProtocolEncoder());
                pipeline.addLast("objectDecoder", new WondexProtocolDecoder(WondexProtocol.this));
            }
        });
    }

}
