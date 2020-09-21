
package org.travice.model;

import java.util.HashSet;
import java.util.Set;

import org.travice.database.QueryIgnore;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Notification extends ScheduledModel {

    private boolean always;

    public boolean getAlways() {
        return always;
    }

    public void setAlways(boolean always) {
        this.always = always;
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    private String notificators;

    public String getNotificators() {
        return notificators;
    }

    public void setNotificators(String transports) {
        this.notificators = transports;
    }


    @JsonIgnore
    @QueryIgnore
    public Set<String> getNotificatorsTypes() {
        final Set<String> result = new HashSet<>();
        if (notificators != null) {
            final String[] transportsList = notificators.split(",");
            for (String transport : transportsList) {
                result.add(transport.trim());
            }
        }
        return result;
    }

}
