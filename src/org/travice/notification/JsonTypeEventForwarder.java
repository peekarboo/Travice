package org.travice.notification;

import java.util.Set;

import org.travice.model.Event;
import org.travice.model.Position;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;

public class JsonTypeEventForwarder extends EventForwarder {

    @Override
    protected void executeRequest(Event event, Position position, Set<Long> users, AsyncInvoker invoker) {
        invoker.post(Entity.json(preparePayload(event, position, users)));
    }

}
