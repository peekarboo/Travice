
package org.travice.model;

public class DeviceState {

    private Boolean motionState;

    public void setMotionState(boolean motionState) {
        this.motionState = motionState;
    }

    public Boolean getMotionState() {
        return motionState;
    }

    private Position motionPosition;

    public void setMotionPosition(Position motionPosition) {
        this.motionPosition = motionPosition;
    }

    public Position getMotionPosition() {
        return motionPosition;
    }

    private Boolean overspeedState;

    public void setOverspeedState(boolean overspeedState) {
        this.overspeedState = overspeedState;
    }

    public Boolean getOverspeedState() {
        return overspeedState;
    }

    private Position overspeedPosition;

    public void setOverspeedPosition(Position overspeedPosition) {
        this.overspeedPosition = overspeedPosition;
    }

    public Position getOverspeedPosition() {
        return overspeedPosition;
    }

    private long overspeedGeofenceId;

    public void setOverspeedGeofenceId(long overspeedGeofenceId) {
        this.overspeedGeofenceId = overspeedGeofenceId;
    }

    public long getOverspeedGeofenceId() {
        return overspeedGeofenceId;
    }

}
