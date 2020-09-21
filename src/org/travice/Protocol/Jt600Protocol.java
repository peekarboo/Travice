
package org.travice.protocol;

import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class Jt600Protocol extends BaseProtocol {

    public Jt600Protocol() {
        super("jt600");
        setSupportedDataCommands(
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_REBOOT_DEVICE);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new Jt600FrameDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new Jt600ProtocolEncoder());
                pipeline.addLast("objectDecoder", new Jt600ProtocolDecoder(Jt600Protocol.this));
            }
        });
    }

}
