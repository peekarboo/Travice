
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class EelinkProtocol extends BaseProtocol {

    public EelinkProtocol() {
        super("eelink");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_REBOOT_DEVICE);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 3, 2));
                pipeline.addLast("objectEncoder", new EelinkProtocolEncoder(false));
                pipeline.addLast("objectDecoder", new EelinkProtocolDecoder(EelinkProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectEncoder", new EelinkProtocolEncoder(true));
                pipeline.addLast("objectDecoder", new EelinkProtocolDecoder(EelinkProtocol.this));
            }
        });
    }

}
