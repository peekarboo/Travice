
package org.travice.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

public class GoogleGeocoder extends JsonGeocoder {

    private static String formatUrl(String key, String language) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f";
        if (key != null) {
            url += "&key=" + key;
        }
        if (language != null) {
            url += "&language=" + language;
        }
        return url;
    }

    public GoogleGeocoder(String key, String language, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(key, language), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray results = json.getJsonArray("results");

        if (!results.isEmpty()) {
            Address address = new Address();

            JsonObject result = (JsonObject) results.get(0);
            JsonArray components = result.getJsonArray("address_components");

            if (result.containsKey("formatted_address")) {
                address.setFormattedAddress(result.getString("formatted_address"));
            }

            for (JsonObject component : components.getValuesAs(JsonObject.class)) {

                String value = component.getString("short_name");

                typesLoop: for (JsonString type : component.getJsonArray("types").getValuesAs(JsonString.class)) {

                    switch (type.getString()) {
                        case "street_number":
                            address.setHouse(value);
                            break typesLoop;
                        case "route":
                            address.setStreet(value);
                            break typesLoop;
                        case "locality":
                            address.setSettlement(value);
                            break typesLoop;
                        case "administrative_area_level_2":
                            address.setDistrict(value);
                            break typesLoop;
                        case "administrative_area_level_1":
                            address.setState(value);
                            break typesLoop;
                        case "country":
                            address.setCountry(value);
                            break typesLoop;
                        case "postal_code":
                            address.setPostcode(value);
                            break typesLoop;
                        default:
                            break;
                    }
                }
            }

            return address;
        }

        return null;
    }

}
