
package org.travice.geocoder;

import org.travice.Context;
import org.travice.helper.Log;

import javax.json.JsonObject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class JsonGeocoder implements Geocoder {

    private final String url;
    private final AddressFormat addressFormat;

    private Map<Map.Entry<Double, Double>, String> cache;

    public JsonGeocoder(String url, final int cacheSize, AddressFormat addressFormat) {
        this.url = url;
        this.addressFormat = addressFormat;
        if (cacheSize > 0) {
            this.cache = Collections.synchronizedMap(new LinkedHashMap<Map.Entry<Double, Double>, String>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > cacheSize;
                }
            });
        }
    }

    private String handleResponse(
            double latitude, double longitude, JsonObject json, ReverseGeocoderCallback callback) {

        Address address = parseAddress(json);
        if (address != null) {
            String formattedAddress = addressFormat.format(address);
            if (cache != null) {
                cache.put(new AbstractMap.SimpleImmutableEntry<>(latitude, longitude), formattedAddress);
            }
            if (callback != null) {
                callback.onSuccess(formattedAddress);
            }
            return formattedAddress;
        } else {
            if (callback != null) {
                callback.onFailure(new GeocoderException("Empty address"));
            } else {
                Log.warning("Empty address");
            }
        }
        return null;
    }

    @Override
    public String getAddress(
            final double latitude, final double longitude, final ReverseGeocoderCallback callback) {

        if (cache != null) {
            String cachedAddress = cache.get(new AbstractMap.SimpleImmutableEntry<>(latitude, longitude));
            if (cachedAddress != null) {
                if (callback != null) {
                    callback.onSuccess(cachedAddress);
                }
                return cachedAddress;
            }
        }

        Invocation.Builder request = Context.getClient().target(String.format(url, latitude, longitude)).request();

        if (callback != null) {
            request.async().get(new InvocationCallback<JsonObject>() {
                @Override
                public void completed(JsonObject json) {
                    handleResponse(latitude, longitude, json, callback);
                }

                @Override
                public void failed(Throwable throwable) {
                    callback.onFailure(throwable);
                }
            });
        } else {
            try {
                return handleResponse(latitude, longitude, request.get(JsonObject.class), null);
            } catch (ClientErrorException e) {
                Log.warning(e);
            }
        }
        return null;
    }

    public abstract Address parseAddress(JsonObject json);

}
