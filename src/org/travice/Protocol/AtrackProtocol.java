
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class AtrackProtocol extends BaseProtocol {

    public AtrackProtocol() {
        super("atrack");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new AtrackFrameDecoder());
                pipeline.addLast("objectEncoder", new AtrackProtocolEncoder());
                pipeline.addLast("objectDecoder", new AtrackProtocolDecoder(AtrackProtocol.this));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectEncoder", new AtrackProtocolEncoder());
                pipeline.addLast("objectDecoder", new AtrackProtocolDecoder(AtrackProtocol.this));
            }
        });
    }

}
