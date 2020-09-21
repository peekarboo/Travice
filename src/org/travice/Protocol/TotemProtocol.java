
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class TotemProtocol extends BaseProtocol {

    public TotemProtocol() {
        super("totem");
        setSupportedDataCommands(
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ENGINE_STOP
        );
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new TotemFrameDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectEncoder", new TotemProtocolEncoder());
                pipeline.addLast("objectDecoder", new TotemProtocolDecoder(TotemProtocol.this));
            }
        });
    }

}
