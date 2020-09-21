
package org.travice.reports.model;

public class TripsConfig {

    public TripsConfig() {
    }

    public TripsConfig(double minimalTripDistance, long minimalTripDuration, long minimalParkingDuration,
            long minimalNoDataDuration, boolean useIgnition, boolean processInvalidPositions, double speedThreshold) {
        this.minimalTripDistance = minimalTripDistance;
        this.minimalTripDuration = minimalTripDuration;
        this.minimalParkingDuration = minimalParkingDuration;
        this.minimalNoDataDuration = minimalNoDataDuration;
        this.useIgnition = useIgnition;
        this.processInvalidPositions = processInvalidPositions;
        this.speedThreshold = speedThreshold;
    }

    private double minimalTripDistance;

    public double getMinimalTripDistance() {
        return minimalTripDistance;
    }

    public void setMinimalTripDistance(double minimalTripDistance) {
        this.minimalTripDistance = minimalTripDistance;
    }

    private long minimalTripDuration;

    public long getMinimalTripDuration() {
        return minimalTripDuration;
    }

    public void setMinimalTripDuration(long minimalTripDuration) {
        this.minimalTripDuration = minimalTripDuration;
    }

    private long minimalParkingDuration;

    public long getMinimalParkingDuration() {
        return minimalParkingDuration;
    }

    public void setMinimalParkingDuration(long minimalParkingDuration) {
        this.minimalParkingDuration = minimalParkingDuration;
    }

    private long minimalNoDataDuration;

    public long getMinimalNoDataDuration() {
        return minimalNoDataDuration;
    }

    public void setMinimalNoDataDuration(long minimalNoDataDuration) {
        this.minimalNoDataDuration = minimalNoDataDuration;
    }

    private boolean useIgnition;

    public boolean getUseIgnition() {
        return useIgnition;
    }

    public void setUseIgnition(boolean useIgnition) {
        this.useIgnition = useIgnition;
    }

    private boolean processInvalidPositions;

    public boolean getProcessInvalidPositions() {
        return processInvalidPositions;
    }

    public void setProcessInvalidPositions(boolean processInvalidPositions) {
        this.processInvalidPositions = processInvalidPositions;
    }

    private double speedThreshold;

    public double getSpeedThreshold() {
        return speedThreshold;
    }

    public void setSpeedThreshold(double speedThreshold) {
        this.speedThreshold = speedThreshold;
    }

}
