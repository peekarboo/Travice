
package org.travice.geocoder;

import javax.json.JsonObject;

public class GisgraphyGeocoder extends JsonGeocoder {

    public GisgraphyGeocoder(AddressFormat addressFormat) {
        this("http://services.gisgraphy.com/reversegeocoding/search", 0, addressFormat);
    }

    public GisgraphyGeocoder(String url, int cacheSize, AddressFormat addressFormat) {
        super(url + "?format=json&lat=%f&lng=%f&from=1&to=1", cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        Address address = new Address();

        JsonObject result = json.getJsonArray("result").getJsonObject(0);

        if (result.containsKey("streetName")) {
            address.setStreet(result.getString("streetName"));
        }
        if (result.containsKey("city")) {
            address.setSettlement(result.getString("city"));
        }
        if (result.containsKey("state")) {
            address.setState(result.getString("state"));
        }
        if (result.containsKey("countryCode")) {
            address.setCountry(result.getString("countryCode"));
        }
        if (result.containsKey("formatedFull")) {
            address.setFormattedAddress(result.getString("formatedFull"));
        }

        return address;
    }

}
