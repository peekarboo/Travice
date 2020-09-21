
package org.travice.events;

import org.travice.Context;
import org.travice.model.Device;
import org.travice.model.Event;

public final class TextMessageEventHandler {

    private TextMessageEventHandler() {
    }

    public static void handleTextMessage(String phone, String message) {
        Device device = Context.getDeviceManager().getDeviceByPhone(phone);
        if (device != null && Context.getNotificationManager() != null) {
            Event event = new Event(Event.TYPE_TEXT_MESSAGE, device.getId());
            event.set("message", message);
            Context.getNotificationManager().updateEvent(event, null);
        }
    }

}
