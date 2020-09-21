
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.CharacterDelimiterFrameDecoder;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;

import java.util.List;
import org.travice.model.Command;

public class SviasProtocol extends BaseProtocol {

    public SviasProtocol() {
        super("svias");
        setSupportedDataCommands(
                Command.TYPE_CUSTOM,
                Command.TYPE_POSITION_SINGLE,
                Command.TYPE_SET_ODOMETER,
                Command.TYPE_ENGINE_STOP,
                Command.TYPE_ENGINE_RESUME,
                Command.TYPE_ALARM_ARM,
                Command.TYPE_ALARM_DISARM,
                Command.TYPE_ALARM_REMOVE);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CharacterDelimiterFrameDecoder(1024, "]"));
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectEncoder", new SviasProtocolEncoder());
                pipeline.addLast("objectDecoder", new SviasProtocolDecoder(SviasProtocol.this));
            }
        });
    }

}
