
package org.travice;

import io.netty.channel.ChannelHandler;
import org.travice.helper.DistanceCalculator;
import org.travice.model.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ChannelHandler.Sharable
public class DistanceHandler extends BaseDataHandler {

    private final boolean filter;
    private final int coordinatesMinError;
    private final int coordinatesMaxError;

    public DistanceHandler(boolean filter, int coordinatesMinError, int coordinatesMaxError) {
        this.filter = filter;
        this.coordinatesMinError = coordinatesMinError;
        this.coordinatesMaxError = coordinatesMaxError;
    }

    private Position getLastPosition(long deviceId) {
        if (Context.getIdentityManager() != null) {
            return Context.getIdentityManager().getLastPosition(deviceId);
        }
        return null;
    }

    @Override
    protected Position handlePosition(Position position) {

        double distance = 0.0;
        if (position.getAttributes().containsKey(Position.KEY_DISTANCE)) {
            distance = position.getDouble(Position.KEY_DISTANCE);
        }
        double totalDistance = 0.0;

        Position last = getLastPosition(position.getDeviceId());
        if (last != null) {
            totalDistance = last.getDouble(Position.KEY_TOTAL_DISTANCE);
            if (!position.getAttributes().containsKey(Position.KEY_DISTANCE)) {
                distance = DistanceCalculator.distance(
                        position.getLatitude(), position.getLongitude(),
                        last.getLatitude(), last.getLongitude());
                distance = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            }
            if (filter && last.getValid() && last.getLatitude() != 0 && last.getLongitude() != 0) {
                boolean satisfiesMin = coordinatesMinError == 0 || distance > coordinatesMinError;
                boolean satisfiesMax = coordinatesMaxError == 0
                        || distance < coordinatesMaxError || position.getValid();
                if (!satisfiesMin || !satisfiesMax) {
                    position.setLatitude(last.getLatitude());
                    position.setLongitude(last.getLongitude());
                    distance = 0;
                }
            }
        }
        position.set(Position.KEY_DISTANCE, distance);
        totalDistance = BigDecimal.valueOf(totalDistance + distance).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        position.set(Position.KEY_TOTAL_DISTANCE, totalDistance);

        return position;
    }

}
