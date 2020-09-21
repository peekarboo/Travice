
package org.travice;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public final class GlobalTimer {

    private static Timer instance = null;

    private GlobalTimer() {
    }

    public static void release() {
        if (instance != null) {
            instance.stop();
        }
        instance = null;
    }

    public static Timer getTimer() {
        if (instance == null) {
            instance = new HashedWheelTimer();
        }
        return instance;
    }

}
