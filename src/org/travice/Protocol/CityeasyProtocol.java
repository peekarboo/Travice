
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class CityeasyProtocol extends BaseProtocol {

    public CityeasyProtocol() {
        super("cityeasy");
        setSupportedDataCommands(
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_POSITION_PERIODIC,
                Command.TYPE_POSITION_STOP,
                Command.TYPE_SET_TIMEZONE);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 2, 2, -4, 0));
                pipeline.addLast("objectEncoder", new CityeasyProtocolEncoder());
                pipeline.addLast("objectDecoder", new CityeasyProtocolDecoder(CityeasyProtocol.this));
            }
        });
    }

}
