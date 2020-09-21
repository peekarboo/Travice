
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.nio.ByteOrder;
import java.util.List;

public class CastelProtocol extends BaseProtocol {

    public CastelProtocol() {
        super("castel");
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder",
                        new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 2, 2, -4, 0, true));
                pipeline.addLast("objectEncoder", new CastelProtocolEncoder());
                pipeline.addLast("objectDecoder", new CastelProtocolDecoder(CastelProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectEncoder", new CastelProtocolEncoder());
                pipeline.addLast("objectDecoder", new CastelProtocolDecoder(CastelProtocol.this));
            }
        });
    }

}
