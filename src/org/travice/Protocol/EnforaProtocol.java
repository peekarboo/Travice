
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class EnforaProtocol extends BaseProtocol {

    public EnforaProtocol() {
        super("enfora");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 0, 2, -2, 2));
                pipeline.addLast("objectEncoder", new EnforaProtocolEncoder());
                pipeline.addLast("objectDecoder", new EnforaProtocolDecoder(EnforaProtocol.this));
            }
        });
    }

}
