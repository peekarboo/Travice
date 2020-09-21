
package org.travice;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.travice.geocoder.Geocoder;
import org.travice.helper.Log;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class GeocoderHandler extends ChannelInboundHandlerAdapter {

    private final Geocoder geocoder;
    private final boolean processInvalidPositions;
    private final int geocoderReuseDistance;

    public GeocoderHandler(Geocoder geocoder, boolean processInvalidPositions) {
        this.geocoder = geocoder;
        this.processInvalidPositions = processInvalidPositions;

        geocoderReuseDistance = Context.getConfig().getInteger("geocoder.reuseDistance", 0);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object message) throws Exception {
        if (message instanceof Position) {
            final Position position = (Position) message;
            if (processInvalidPositions || position.getValid()) {
                if (geocoderReuseDistance != 0) {
                    Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
                    if (lastPosition != null && lastPosition.getAddress() != null
                            && position.getDouble(Position.KEY_DISTANCE) <= geocoderReuseDistance) {
                        position.setAddress(lastPosition.getAddress());
                        ctx.fireChannelRead(position);
                        return;
                    }
                }

                Context.getStatisticsManager().registerGeocoderRequest();

                geocoder.getAddress(position.getLatitude(), position.getLongitude(),
                        new Geocoder.ReverseGeocoderCallback() {
                    @Override
                    public void onSuccess(String address) {
                        position.setAddress(address);
                        ctx.fireChannelRead(position);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.warning("Geocoding failed", e);
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
