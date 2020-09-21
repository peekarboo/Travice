
package org.travice.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.nio.ByteOrder;
import java.util.List;

public class AdmProtocol extends BaseProtocol {

    public AdmProtocol() {
        super("adm");
        setSupportedDataCommands(
                Command.TYPE_GET_DEVICE_STATUS,
                Command.TYPE_CUSTOM);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder",
                        new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 2, 1, -3, 0, true));
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("objectEncoder", new AdmProtocolEncoder());
                pipeline.addLast("objectDecoder", new AdmProtocolDecoder(AdmProtocol.this));
            }
        });
    }

}
