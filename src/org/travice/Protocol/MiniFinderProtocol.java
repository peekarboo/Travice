
package org.travice.protocol;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.travice.BaseProtocol;
import org.travice.CharacterDelimiterFrameDecoder;
import org.travice.PipelineBuilder;
import org.travice.TrackerServer;
import org.travice.model.Command;

import java.util.List;

public class MiniFinderProtocol extends BaseProtocol {

    public MiniFinderProtocol() {
        super("minifinder");
        setSupportedDataCommands(
                Command.TYPE_SET_TIMEZONE,
                Command.TYPE_VOICE_MONITORING,
                Command.TYPE_ALARM_SPEED,
                Command.TYPE_ALARM_GEOFENCE,
                Command.TYPE_ALARM_VIBRATION,
                Command.TYPE_SET_AGPS,
                Command.TYPE_ALARM_FALL,
                Command.TYPE_MODE_POWER_SAVING,
                Command.TYPE_MODE_DEEP_SLEEP,
                Command.TYPE_SOS_NUMBER,
                Command.TYPE_SET_INDICATOR);
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        serverList.add(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast("frameDecoder", new CharacterDelimiterFrameDecoder(1024, ';'));
                pipeline.addLast("stringEncoder", new StringEncoder());
                pipeline.addLast("stringDecoder", new StringDecoder());
                pipeline.addLast("objectEncoder", new MiniFinderProtocolEncoder());
                pipeline.addLast("objectDecoder", new MiniFinderProtocolDecoder(MiniFinderProtocol.this));
            }
        });
    }

}
