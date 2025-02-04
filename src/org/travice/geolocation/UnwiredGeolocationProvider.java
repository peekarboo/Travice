
package org.travice.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.travice.Context;
import org.travice.model.CellTower;
import org.travice.model.Network;
import org.travice.model.WifiAccessPoint;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import java.util.Collection;

public class UnwiredGeolocationProvider implements GeolocationProvider {

    private String url;
    private String key;

    private ObjectMapper objectMapper;

    private abstract static class NetworkMixIn {
        @JsonProperty("mcc")
        abstract Integer getHomeMobileCountryCode();
        @JsonProperty("mnc")
        abstract Integer getHomeMobileNetworkCode();
        @JsonProperty("radio")
        abstract String getRadioType();
        @JsonIgnore
        abstract String getCarrier();
        @JsonIgnore
        abstract Boolean getConsiderIp();
        @JsonProperty("cells")
        abstract Collection<CellTower> getCellTowers();
        @JsonProperty("wifi")
        abstract Collection<WifiAccessPoint> getWifiAccessPoints();
    }

    private abstract static class CellTowerMixIn {
        @JsonProperty("radio")
        abstract String getRadioType();
        @JsonProperty("mcc")
        abstract Integer getMobileCountryCode();
        @JsonProperty("mnc")
        abstract Integer getMobileNetworkCode();
        @JsonProperty("lac")
        abstract Integer getLocationAreaCode();
        @JsonProperty("cid")
        abstract Long getCellId();
    }

    private abstract static class WifiAccessPointMixIn {
        @JsonProperty("bssid")
        abstract String getMacAddress();
        @JsonProperty("signal")
        abstract Integer getSignalStrength();
    }

    public UnwiredGeolocationProvider(String url, String key) {
        this.url = url;
        this.key = key;

        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Network.class, NetworkMixIn.class);
        objectMapper.addMixIn(CellTower.class, CellTowerMixIn.class);
        objectMapper.addMixIn(WifiAccessPoint.class, WifiAccessPointMixIn.class);
    }

    @Override
    public void getLocation(Network network, final LocationProviderCallback callback) {
        ObjectNode json = objectMapper.valueToTree(network);
        json.put("token", key);

        Context.getClient().target(url).request().async().post(Entity.json(json), new InvocationCallback<JsonObject>() {
            @Override
            public void completed(JsonObject json) {
                if (json.getString("status").equals("error")) {
                    callback.onFailure(new GeolocationException(json.getString("message")));
                } else {
                    callback.onSuccess(
                            json.getJsonNumber("lat").doubleValue(),
                            json.getJsonNumber("lon").doubleValue(),
                            json.getJsonNumber("accuracy").doubleValue());
                }
            }

            @Override
            public void failed(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

}
