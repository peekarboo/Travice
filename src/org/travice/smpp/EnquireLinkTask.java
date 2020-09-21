
package org.travice.smpp;

import org.travice.helper.Log;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;

public class EnquireLinkTask implements Runnable {

    private SmppClient smppClient;
    private Integer enquireLinkTimeout;

    public EnquireLinkTask(SmppClient smppClient, Integer enquireLinkTimeout) {
        this.smppClient = smppClient;
        this.enquireLinkTimeout = enquireLinkTimeout;
    }

    @Override
    public void run() {
        SmppSession smppSession = smppClient.getSession();
        if (smppSession != null && smppSession.isBound()) {
            try {
                smppSession.enquireLink(new EnquireLink(), enquireLinkTimeout);
            } catch (SmppTimeoutException | SmppChannelException
                    | RecoverablePduException | UnrecoverablePduException error) {
                Log.warning("Enquire link failed, executing reconnect: ", error);
                smppClient.scheduleReconnect();
            } catch (InterruptedException error) {
                Log.info("Enquire link interrupted, probably killed by reconnecting");
            }
        } else {
            Log.warning("Enquire link running while session is not connected");
        }
    }

}
