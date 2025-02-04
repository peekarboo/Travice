
package org.travice;

import io.netty.channel.ChannelHandler;
import org.travice.helper.Log;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class FilterHandler extends BaseDataHandler {

    private boolean filterInvalid;
    private boolean filterZero;
    private boolean filterDuplicate;
    private long filterFuture;
    private boolean filterApproximate;
    private int filterAccuracy;
    private boolean filterStatic;
    private int filterDistance;
    private int filterMaxSpeed;
    private long filterMinPeriod;
    private long skipLimit;
    private boolean skipAttributes;

    public void setFilterInvalid(boolean filterInvalid) {
        this.filterInvalid = filterInvalid;
    }

    public void setFilterZero(boolean filterZero) {
        this.filterZero = filterZero;
    }

    public void setFilterDuplicate(boolean filterDuplicate) {
        this.filterDuplicate = filterDuplicate;
    }

    public void setFilterFuture(long filterFuture) {
        this.filterFuture = filterFuture;
    }

    public void setFilterAccuracy(int filterAccuracy) {
        this.filterAccuracy = filterAccuracy;
    }

    public void setFilterApproximate(boolean filterApproximate) {
        this.filterApproximate = filterApproximate;
    }

    public void setFilterStatic(boolean filterStatic) {
        this.filterStatic = filterStatic;
    }

    public void setFilterDistance(int filterDistance) {
        this.filterDistance = filterDistance;
    }

    public void setFilterMaxSpeed(int filterMaxSpeed) {
        this.filterMaxSpeed = filterMaxSpeed;
    }

    public void setFilterMinPeriod(int filterMinPeriod) {
        this.filterMinPeriod = filterMinPeriod;
    }

    public void setSkipLimit(long skipLimit) {
        this.skipLimit = skipLimit;
    }

    public void setSkipAttributes(boolean skipAttributes) {
        this.skipAttributes = skipAttributes;
    }

    public FilterHandler() {
        Config config = Context.getConfig();
        if (config != null) {
            filterInvalid = config.getBoolean("filter.invalid");
            filterZero = config.getBoolean("filter.zero");
            filterDuplicate = config.getBoolean("filter.duplicate");
            filterFuture = config.getLong("filter.future") * 1000;
            filterAccuracy = config.getInteger("filter.accuracy");
            filterApproximate = config.getBoolean("filter.approximate");
            filterStatic = config.getBoolean("filter.static");
            filterDistance = config.getInteger("filter.distance");
            filterMaxSpeed = config.getInteger("filter.maxSpeed");
            filterMinPeriod = config.getInteger("filter.minPeriod") * 1000;
            skipLimit = config.getLong("filter.skipLimit") * 1000;
            skipAttributes = config.getBoolean("filter.skipAttributes.enable");
        }
    }

    private boolean filterInvalid(Position position) {
        return filterInvalid && (!position.getValid()
           || position.getLatitude() > 90 || position.getLongitude() > 180
           || position.getLatitude() < -90 || position.getLongitude() < -180);
    }

    private boolean filterZero(Position position) {
        return filterZero && position.getLatitude() == 0.0 && position.getLongitude() == 0.0;
    }

    private boolean filterDuplicate(Position position, Position last) {
        if (filterDuplicate && last != null && position.getFixTime().equals(last.getFixTime())) {
            for (String key : position.getAttributes().keySet()) {
                if (!last.getAttributes().containsKey(key)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean filterFuture(Position position) {
        return filterFuture != 0 && position.getFixTime().getTime() > System.currentTimeMillis() + filterFuture;
    }

    private boolean filterAccuracy(Position position) {
        return filterAccuracy != 0 && position.getAccuracy() > filterAccuracy;
    }

    private boolean filterApproximate(Position position) {
        return filterApproximate && position.getBoolean(Position.KEY_APPROXIMATE);
    }

    private boolean filterStatic(Position position) {
        return filterStatic && position.getSpeed() == 0.0;
    }

    private boolean filterDistance(Position position, Position last) {
        if (filterDistance != 0 && last != null) {
            return position.getDouble(Position.KEY_DISTANCE) < filterDistance;
        }
        return false;
    }

    private boolean filterMaxSpeed(Position position, Position last) {
        if (filterMaxSpeed != 0 && last != null) {
            double distance = position.getDouble(Position.KEY_DISTANCE);
            double time = position.getFixTime().getTime() - last.getFixTime().getTime();
            return UnitsConverter.knotsFromMps(distance / (time / 1000)) > filterMaxSpeed;
        }
        return false;
    }

    private boolean filterMinPeriod(Position position, Position last) {
        if (filterMinPeriod != 0 && last != null) {
            long time = position.getFixTime().getTime() - last.getFixTime().getTime();
            return time > 0 && time < filterMinPeriod;
        }
        return false;
    }

    private boolean skipLimit(Position position, Position last) {
        if (skipLimit != 0 && last != null) {
            return (position.getServerTime().getTime() - last.getServerTime().getTime()) > skipLimit;
        }
        return false;
    }

    private boolean skipAttributes(Position position) {
        if (skipAttributes) {
            String attributesString = Context.getIdentityManager().lookupAttributeString(
                    position.getDeviceId(), "filter.skipAttributes", "", true);
            for (String attribute : attributesString.split("[ ,]")) {
                if (position.getAttributes().containsKey(attribute)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean filter(Position position) {

        StringBuilder filterType = new StringBuilder();

        Position last = null;
        if (Context.getIdentityManager() != null) {
            last = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        }

        if (skipLimit(position, last) || skipAttributes(position)) {
            return false;
        }

        if (filterInvalid(position)) {
            filterType.append("Invalid ");
        }
        if (filterZero(position)) {
            filterType.append("Zero ");
        }
        if (filterDuplicate(position, last)) {
            filterType.append("Duplicate ");
        }
        if (filterFuture(position)) {
            filterType.append("Future ");
        }
        if (filterAccuracy(position)) {
            filterType.append("Accuracy ");
        }
        if (filterApproximate(position)) {
            filterType.append("Approximate ");
        }
        if (filterStatic(position)) {
            filterType.append("Static ");
        }
        if (filterDistance(position, last)) {
            filterType.append("Distance ");
        }
        if (filterMaxSpeed(position, last)) {
            filterType.append("MaxSpeed ");
        }
        if (filterMinPeriod(position, last)) {
            filterType.append("MinPeriod ");
        }

        if (filterType.length() > 0) {

            StringBuilder message = new StringBuilder();
            message.append("Position filtered by ");
            message.append(filterType.toString());
            message.append("filters from device: ");
            message.append(Context.getIdentityManager().getById(position.getDeviceId()).getUniqueId());

            Log.info(message.toString());
            return true;
        }

        return false;
    }

    @Override
    protected Position handlePosition(Position position) {
        if (filter(position)) {
            return null;
        }
        return position;
    }

}
