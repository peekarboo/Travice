
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class TeltonikaProtocol extends BaseProtocol {

    public TeltonikaProtocol() {
        super("teltonika");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new TeltonikaFrameDecoder());
                pipeline.addLast("objectEncoder", new TeltonikaProtocolEncoder());
                pipeline.addLast("objectDecoder", new TeltonikaProtocolDecoder(TeltonikaProtocol.this, false));
            }
        });
        serverList.add(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("objectEncoder", new TeltonikaProtocolEncoder());
                pipeline.addLast("objectDecoder", new TeltonikaProtocolDecoder(TeltonikaProtocol.this, true));
            }
        });
    }

}
