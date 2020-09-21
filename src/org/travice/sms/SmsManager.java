
package org.travice.sms;

import org.travice.notification.MessageException;

public interface SmsManager {

    void sendMessageSync(String destAddress, String message, boolean command)
            throws InterruptedException, MessageException;
    void sendMessageAsync(final String destAddress, final String message, final boolean command);

}
