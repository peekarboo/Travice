
package org.travice;

import java.util.TimeZone;

public class DeviceSession {

    private final long deviceId;

    public DeviceSession(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    private TimeZone timeZone;

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

}
