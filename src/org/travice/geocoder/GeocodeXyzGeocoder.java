
package org.travice.geocoder;

import javax.json.JsonObject;

public class GeocodeXyzGeocoder extends JsonGeocoder {

    private static String formatUrl(String key) {
        String url = "https://geocode.xyz/%f,%f?geoit=JSON";
        if (key != null) {
            url += "&key=" + key;
        }
        return url;
    }

    public GeocodeXyzGeocoder(String key, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(key), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        Address address = new Address();

        if (json.containsKey("stnumber")) {
            address.setHouse(json.getString("stnumber"));
        }
        if (json.containsKey("staddress")) {
            address.setStreet(json.getString("staddress"));
        }
        if (json.containsKey("city")) {
            address.setSettlement(json.getString("city"));
        }
        if (json.containsKey("region")) {
            address.setState(json.getString("region"));
        }
        if (json.containsKey("prov")) {
            address.setCountry(json.getString("prov"));
        }
        if (json.containsKey("postal")) {
            address.setPostcode(json.getString("postal"));
        }

        return address;
    }

}
