
package org.travice;

import io.netty.channel.ChannelHandler;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class EngineHoursHandler extends BaseDataHandler {

    @Override
    protected Position handlePosition(Position position) {
        if (!position.getAttributes().containsKey(Position.KEY_HOURS)) {
            Position last = Context.getIdentityManager().getLastPosition(position.getDeviceId());
            if (last != null) {
                long hours = last.getLong(Position.KEY_HOURS);
                if (last.getBoolean(Position.KEY_IGNITION) && position.getBoolean(Position.KEY_IGNITION)) {
                    hours += position.getFixTime().getTime() - last.getFixTime().getTime();
                }
                if (hours != 0) {
                    position.set(Position.KEY_HOURS, hours);
                }
            }
        }
        return position;
    }

}
