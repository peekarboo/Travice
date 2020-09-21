
package org.travice.smpp;

public class ReconnectionTask implements Runnable {

    private final SmppClient smppClient;

    protected ReconnectionTask(SmppClient smppClient) {
        this.smppClient = smppClient;
    }

    @Override
    public void run() {
        smppClient.reconnect();
    }
}
