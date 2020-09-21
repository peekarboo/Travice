
package org.travice.geolocation;

public class GoogleGeolocationProvider extends UniversalGeolocationProvider {

    private static final String URL = "https://www.googleapis.com/geolocation/v1/geolocate";

    public GoogleGeolocationProvider(String key) {
        super(URL, key);
    }

}
