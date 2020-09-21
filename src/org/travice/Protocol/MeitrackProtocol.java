
package org.travice.protocol;

import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class MeitrackProtocol extends BaseProtocol {

    public MeitrackProtocol() {
        super("meitrack");
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM,
                Command.TYPE_REQUEST_PHOTO,
                Command.TYPE_SEND_SMS);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new MeitrackFrameDecoder());
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new MeitrackProtocolEncoder());
                pipeline.addLast("objectDecoder", new MeitrackProtocolDecoder(MeitrackProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new MeitrackProtocolEncoder());
                pipeline.addLast("objectDecoder", new MeitrackProtocolDecoder(MeitrackProtocol.this));
            }
        });
    }

}
