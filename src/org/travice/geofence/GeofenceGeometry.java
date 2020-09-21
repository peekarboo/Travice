
package org.travice.geofence;

import java.text.ParseException;

public abstract class GeofenceGeometry {

    public abstract boolean containsPoint(double latitude, double longitude);

    public abstract String toWkt();

    public abstract void fromWkt(String wkt) throws ParseException;

    public static class Coordinate {

        private double lat;
        private double lon;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }

}
