
package org.travice.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class BingMapsGeocoder extends JsonGeocoder {

    public BingMapsGeocoder(String url, String key, int cacheSize, AddressFormat addressFormat) {
        super(url + "/Locations/%f,%f?key=" + key + "&include=ciso2", cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray result = json.getJsonArray("resourceSets");
        if (result != null) {
            JsonObject location =
                    result.getJsonObject(0).getJsonArray("resources").getJsonObject(0).getJsonObject("address");
            if (location != null) {
                Address address = new Address();
                if (location.containsKey("addressLine")) {
                    address.setStreet(location.getString("addressLine"));
                }
                if (location.containsKey("locality")) {
                    address.setSettlement(location.getString("locality"));
                }
                if (location.containsKey("adminDistrict2")) {
                    address.setDistrict(location.getString("adminDistrict2"));
                }
                if (location.containsKey("adminDistrict")) {
                    address.setState(location.getString("adminDistrict"));
                }
                if (location.containsKey("countryRegionIso2")) {
                    address.setCountry(location.getString("countryRegionIso2").toUpperCase());
                }
                if (location.containsKey("postalCode")) {
                    address.setPostcode(location.getString("postalCode"));
                }
                if (location.containsKey("formattedAddress")) {
                    address.setFormattedAddress(location.getString("formattedAddress"));
                }
                return address;
            }
        }
        return null;
    }

}
