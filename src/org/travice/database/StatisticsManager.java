
package org.travice.database;

import org.joda.time.format.ISODateTimeFormat;
import org.travice.Context;
import org.travice.helper.Log;
import org.travice.model.Statistics;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsManager {

    private static final int SPLIT_MODE = Calendar.DAY_OF_MONTH;

    private AtomicInteger lastUpdate = new AtomicInteger(Calendar.getInstance().get(SPLIT_MODE));

    private Set<Long> users = new HashSet<>();
    private Set<Long> devices = new HashSet<>();

    private int requests;
    private int messagesReceived;
    private int messagesStored;
    private int mailSent;
    private int smsSent;
    private int geocoderRequests;
    private int geolocationRequests;

    private void checkSplit() {
        int currentUpdate = Calendar.getInstance().get(SPLIT_MODE);
        if (lastUpdate.getAndSet(currentUpdate) != currentUpdate) {
            Statistics statistics = new Statistics();
            statistics.setCaptureTime(new Date());
            statistics.setActiveUsers(users.size());
            statistics.setActiveDevices(devices.size());
            statistics.setRequests(requests);
            statistics.setMessagesReceived(messagesReceived);
            statistics.setMessagesStored(messagesStored);
            statistics.setMailSent(mailSent);
            statistics.setSmsSent(smsSent);
            statistics.setGeocoderRequests(geocoderRequests);
            statistics.setGeolocationRequests(geolocationRequests);

            try {
                Context.getDataManager().addObject(statistics);
            } catch (SQLException e) {
                Log.warning(e);
            }

            String url = Context.getConfig().getString("server.statistics");
            if (url != null) {
                String time = ISODateTimeFormat.dateTime().print(statistics.getCaptureTime().getTime());

                Form form = new Form();
                form.param("version", Log.getAppVersion());
                form.param("captureTime", time);
                form.param("activeUsers", String.valueOf(statistics.getActiveUsers()));
                form.param("activeDevices", String.valueOf(statistics.getActiveDevices()));
                form.param("requests", String.valueOf(statistics.getRequests()));
                form.param("messagesReceived", String.valueOf(statistics.getMessagesReceived()));
                form.param("messagesStored", String.valueOf(statistics.getMessagesStored()));
                form.param("mailSent", String.valueOf(statistics.getMailSent()));
                form.param("smsSent", String.valueOf(statistics.getSmsSent()));
                form.param("geocoderRequests", String.valueOf(statistics.getGeocoderRequests()));
                form.param("geolocationRequests", String.valueOf(statistics.getGeolocationRequests()));

                Context.getClient().target(url).request().async().post(Entity.form(form));
            }

            users.clear();
            devices.clear();
            requests = 0;
            messagesReceived = 0;
            messagesStored = 0;
            mailSent = 0;
            smsSent = 0;
            geocoderRequests = 0;
            geolocationRequests = 0;
        }
    }

    public synchronized void registerRequest(long userId) {
        checkSplit();
        requests += 1;
        if (userId != 0) {
            users.add(userId);
        }
    }

    public synchronized void registerMessageReceived() {
        checkSplit();
        messagesReceived += 1;
    }

    public synchronized void registerMessageStored(long deviceId) {
        checkSplit();
        messagesStored += 1;
        if (deviceId != 0) {
            devices.add(deviceId);
        }
    }

    public synchronized void registerMail() {
        checkSplit();
        mailSent += 1;
    }

    public synchronized void registerSms() {
        checkSplit();
        smsSent += 1;
    }

    public synchronized void registerGeocoderRequest() {
        checkSplit();
        geocoderRequests += 1;
    }

    public synchronized void registerGeolocationRequest() {
        checkSplit();
        geolocationRequests += 1;
    }

}
