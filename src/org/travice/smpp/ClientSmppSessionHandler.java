
package org.travice.smpp;

import org.travice.events.TextMessageEventHandler;
import org.travice.helper.Log;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.util.SmppUtil;

public class ClientSmppSessionHandler extends DefaultSmppSessionHandler {

    private SmppClient smppClient;

    public ClientSmppSessionHandler(SmppClient smppClient) {
        this.smppClient = smppClient;
    }

    @Override
    public void firePduRequestExpired(PduRequest pduRequest) {
        Log.warning("PDU request expired: " + pduRequest);
    }

    @Override
    public PduResponse firePduRequestReceived(PduRequest request) {
        PduResponse response = null;
        try {
            if (request instanceof DeliverSm) {
                String sourceAddress = ((DeliverSm) request).getSourceAddress().getAddress();
                String message = CharsetUtil.decode(((DeliverSm) request).getShortMessage(),
                        smppClient.mapDataCodingToCharset(((DeliverSm) request).getDataCoding()));
                Log.debug("SMS Message Received: " + message.trim() + ", Source Address: " + sourceAddress);

                boolean isDeliveryReceipt = false;
                if (smppClient.getDetectDlrByOpts()) {
                    isDeliveryReceipt = request.getOptionalParameters() != null;
                } else {
                    isDeliveryReceipt = SmppUtil.isMessageTypeAnyDeliveryReceipt(((DeliverSm) request).getEsmClass());
                }

                if (!isDeliveryReceipt) {
                    TextMessageEventHandler.handleTextMessage(sourceAddress, message);
                }
            }
            response = request.createResponse();
        } catch (Throwable error) {
            Log.warning(error);
            response = request.createResponse();
            response.setResultMessage(error.getMessage());
            response.setCommandStatus(SmppConstants.STATUS_UNKNOWNERR);
        }
        return response;
    }

    @Override
    public void fireChannelUnexpectedlyClosed() {
        Log.warning("SMPP session channel unexpectedly closed");
        smppClient.scheduleReconnect();
    }
}
