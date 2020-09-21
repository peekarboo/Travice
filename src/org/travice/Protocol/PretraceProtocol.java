
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.CharacterDelimiterFrameDecoder;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class PretraceProtocol extends BaseProtocol {

    public PretraceProtocol() {
        super("pretrace");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_POSITION_PERIODIC);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CharacterDelimiterFrameDecoder(1024, ')'));
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectEncoder", new PretraceProtocolEncoder());
                pipeline.addLast("objectDecoder", new PretraceProtocolDecoder(PretraceProtocol.this));
            }
        });
    }

}
