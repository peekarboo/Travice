
package org.travice.geocoder;

import javax.json.JsonObject;

public class FactualGeocoder extends JsonGeocoder {

    public FactualGeocoder(String url, String key, int cacheSize, AddressFormat addressFormat) {
        super(url + "?latitude=%f&longitude=%f&KEY=" + key, cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonObject result = json.getJsonObject("response").getJsonObject("data");
        if (result != null) {
                Address address = new Address();
                if (result.getJsonObject("street_number") != null) {
                    address.setHouse(result.getJsonObject("street_number").getString("name"));
                }
                if (result.getJsonObject("street_name") != null) {
                    address.setStreet(result.getJsonObject("street_name").getString("name"));
                }
                if (result.getJsonObject("locality") != null) {
                    address.setSettlement(result.getJsonObject("locality").getString("name"));
                }
                if (result.getJsonObject("county") != null) {
                    address.setDistrict(result.getJsonObject("county").getString("name"));
                }
                if (result.getJsonObject("region") != null) {
                    address.setState(result.getJsonObject("region").getString("name"));
                }
                if (result.getJsonObject("country") != null) {
                    address.setCountry(result.getJsonObject("country").getString("name"));
                }
                if (result.getJsonObject("postcode") != null) {
                    address.setPostcode(result.getJsonObject("postcode").getString("name"));
                }
                return address;
        }
        return null;
    }

}
