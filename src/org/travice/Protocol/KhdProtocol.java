
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class KhdProtocol extends BaseProtocol {

    public KhdProtocol() {
        super("khd");
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(512, 3, 2));
                pipeline.addLast("objectEncoder", new KhdProtocolEncoder());
                pipeline.addLast("objectDecoder", new KhdProtocolDecoder(KhdProtocol.this));
            }
        });
    }

}
