
package org.travice.protocol;

import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class Pt502Protocol extends BaseProtocol {

    public Pt502Protocol() {
        super("pt502");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_ALARM_SPEED,
                Command.TYPE_OUTPUT_CONTROL,
                Command.TYPE_REQUEST_PHOTO);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new Pt502FrameDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new Pt502ProtocolEncoder());
                pipeline.addLast("objectDecoder", new Pt502ProtocolDecoder(Pt502Protocol.this));
            }
        });
    }

}
