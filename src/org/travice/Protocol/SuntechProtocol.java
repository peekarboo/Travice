
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.CharacterDelimiterFrameDecoder;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class SuntechProtocol extends BaseProtocol {

    public SuntechProtocol() {
        super("suntech");
        setSupportedDataCommands(
                Command.TYPE_OUTPUT_CONTROL,
                Command.TYPE_REBOOT_DEVICE,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CharacterDelimiterFrameDecoder(1024, '\r'));
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectEncoder", new SuntechProtocolEncoder());
                pipeline.addLast("objectDecoder", new SuntechProtocolDecoder(SuntechProtocol.this));
            }
        });
    }

}
