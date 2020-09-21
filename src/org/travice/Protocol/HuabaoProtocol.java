
package org.travice.protocol;

import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class HuabaoProtocol extends BaseProtocol {

    public HuabaoProtocol() {
        super("huabao");
        setSupportedDataCommands(
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new HuabaoFrameDecoder());
                pipeline.addLast("objectEncoder", new HuabaoProtocolEncoder());
                pipeline.addLast("objectDecoder", new HuabaoProtocolDecoder(HuabaoProtocol.this));
            }
        });
    }

}
