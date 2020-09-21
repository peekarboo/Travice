
package org.travice.protocol;

import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.Context;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class H02Protocol extends BaseProtocol {

    public H02Protocol() {
        super("h02");
        setSupportedDataCommands(
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_POSITION_PERIODIC
        );
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                int messageLength = Context.getConfig().getInteger(getName() + ".messageLength");
                pipeline.addLast("frameDecoder", new H02FrameDecoder(messageLength));
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new H02ProtocolEncoder());
                pipeline.addLast("objectDecoder", new H02ProtocolDecoder(H02Protocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new H02ProtocolEncoder());
                pipeline.addLast("objectDecoder", new H02ProtocolDecoder(H02Protocol.this));
            }
        });
    }
}
