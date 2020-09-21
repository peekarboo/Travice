
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class RuptelaProtocol extends BaseProtocol {

    public RuptelaProtocol() {
        super("ruptela");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_CONFIGURATION,
                Command.TYPE_GET_VERSION,
                Command.TYPE_FIRMWARE_UPDATE,
                Command.TYPE_OUTPUT_CONTROL,
                Command.TYPE_SET_CONNECTION,
                Command.TYPE_SET_ODOMETER);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 0, 2, 2, 0));
                pipeline.addLast("objectEncoder", new RuptelaProtocolEncoder());
                pipeline.addLast("objectDecoder", new RuptelaProtocolDecoder(RuptelaProtocol.this));
            }
        });
    }

}
