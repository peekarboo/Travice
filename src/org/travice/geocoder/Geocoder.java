
package org.travice.geocoder;

public interface Geocoder {

    interface ReverseGeocoderCallback {

        void onSuccess(String address);

        void onFailure(Throwable e);

    }

    String getAddress(double latitude, double longitude, ReverseGeocoderCallback callback);

}
