
package org.travice.events;

import java.util.Collections;
import java.util.Map;

import io.netty.channel.ChannelHandler;
import org.travice.BaseEventHandler;
import org.travice.model.Event;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class CommandResultEventHandler extends BaseEventHandler {

    @Override
    protected Map<Event, Position> analyzePosition(Position position) {
        Object commandResult = position.getAttributes().get(Position.KEY_RESULT);
        if (commandResult != null) {
            Event event = new Event(Event.TYPE_COMMAND_RESULT, position.getDeviceId(), position.getId());
            event.set(Position.KEY_RESULT, (String) commandResult);
            return Collections.singletonMap(event, position);
        }
        return null;
    }

}
