
package org.travice;

import java.util.Map;

import org.travice.model.Event;
import org.travice.model.Position;

public abstract class BaseEventHandler extends BaseDataHandler {

    @Override
    protected Position handlePosition(Position position) {
        Map<Event, Position> events = analyzePosition(position);
        if (events != null && Context.getNotificationManager() != null) {
            Context.getNotificationManager().updateEvents(events);
        }
        return position;
    }

    protected abstract Map<Event, Position> analyzePosition(Position position);

}
