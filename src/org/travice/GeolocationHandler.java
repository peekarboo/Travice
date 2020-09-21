
package org.travice;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.travice.helper.Log;
import org.travice.geolocation.GeolocationProvider;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class GeolocationHandler extends ChannelInboundHandlerAdapter {

    private final GeolocationProvider geolocationProvider;
    private final boolean processInvalidPositions;

    public GeolocationHandler(GeolocationProvider geolocationProvider, boolean processInvalidPositions) {
        this.geolocationProvider = geolocationProvider;
        this.processInvalidPositions = processInvalidPositions;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object message) throws Exception {
        if (message instanceof Position) {
            final Position position = (Position) message;
            if ((position.getOutdated() || processInvalidPositions && !position.getValid())
                    && position.getNetwork() != null) {
                Context.getStatisticsManager().registerGeolocationRequest();

                geolocationProvider.getLocation(position.getNetwork(),
                        new GeolocationProvider.LocationProviderCallback() {
                    @Override
                    public void onSuccess(double latitude, double longitude, double accuracy) {
                        position.set(Position.KEY_APPROXIMATE, true);
                        position.setValid(true);
                        position.setFixTime(position.getDeviceTime());
                        position.setLatitude(latitude);
                        position.setLongitude(longitude);
                        position.setAccuracy(accuracy);
                        position.setAltitude(0);
                        position.setSpeed(0);
                        position.setCourse(0);
                        position.set(Position.KEY_RSSI, 0);
                        ctx.fireChannelRead(position);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.warning(e);
                        ctx.fireChannelRead(position);
                    }
                });
            } else {
                ctx.fireChannelRead(position);
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }

}
