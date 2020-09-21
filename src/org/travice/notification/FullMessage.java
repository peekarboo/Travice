
package org.travice.notification;

public class FullMessage {

    private String subject;
    private String body;

    public FullMessage(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
