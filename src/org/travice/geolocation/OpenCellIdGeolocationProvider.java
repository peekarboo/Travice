
package org.travice.geolocation;

import org.travice.Context;
import org.travice.model.CellTower;
import org.travice.model.Network;

import javax.json.JsonObject;
import javax.ws.rs.client.InvocationCallback;

public class OpenCellIdGeolocationProvider implements GeolocationProvider {

    private String url;

    public OpenCellIdGeolocationProvider(String key) {
        this("http://opencellid.org/cell/get", key);
    }

    public OpenCellIdGeolocationProvider(String url, String key) {
        this.url = url + "?format=json&mcc=%d&mnc=%d&lac=%d&cellid=%d&key=" + key;
    }

    @Override
    public void getLocation(Network network, final LocationProviderCallback callback) {
        if (network.getCellTowers() != null && !network.getCellTowers().isEmpty()) {

            CellTower cellTower = network.getCellTowers().iterator().next();
            String request = String.format(url, cellTower.getMobileCountryCode(), cellTower.getMobileNetworkCode(),
                    cellTower.getLocationAreaCode(), cellTower.getCellId());

            Context.getClient().target(request).request().async().get(new InvocationCallback<JsonObject>() {
                @Override
                public void completed(JsonObject json) {
                    if (json.containsKey("lat") && json.containsKey("lon")) {
                        callback.onSuccess(
                                json.getJsonNumber("lat").doubleValue(),
                                json.getJsonNumber("lon").doubleValue(), 0);
                    } else {
                        callback.onFailure(new GeolocationException("Coordinates are missing"));
                    }
                }

                @Override
                public void failed(Throwable throwable) {
                    callback.onFailure(throwable);
                }
            });

        } else {
            callback.onFailure(new GeolocationException("No network information"));
        }
    }

}
