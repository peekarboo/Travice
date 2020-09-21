
package org.travice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.util.URIUtil;
import org.travice.database.CalendarManager;
import org.travice.database.CommandsManager;
import org.travice.database.AttributesManager;
import org.travice.database.BaseObjectManager;
import org.travice.database.ConnectionManager;
import org.travice.database.DataManager;
import org.travice.database.DeviceManager;
import org.travice.database.DriversManager;
import org.travice.database.IdentityManager;
import org.travice.database.LdapProvider;
import org.travice.database.MaintenancesManager;
import org.travice.database.MediaManager;
import org.travice.database.NotificationManager;
import org.travice.database.PermissionsManager;
import org.travice.database.GeofenceManager;
import org.travice.database.GroupsManager;
import org.travice.database.StatisticsManager;
import org.travice.database.UsersManager;
import org.travice.events.MotionEventHandler;
import org.travice.events.OverspeedEventHandler;
import org.travice.geocoder.AddressFormat;
import org.travice.geocoder.BingMapsGeocoder;
import org.travice.geocoder.FactualGeocoder;
import org.travice.geocoder.GeocodeFarmGeocoder;
import org.travice.geocoder.GeocodeXyzGeocoder;
import org.travice.geocoder.GisgraphyGeocoder;
import org.travice.geocoder.GoogleGeocoder;
import org.travice.geocoder.MapQuestGeocoder;
import org.travice.geocoder.NominatimGeocoder;
import org.travice.geocoder.OpenCageGeocoder;
import org.travice.geocoder.Geocoder;
import org.travice.geolocation.UnwiredGeolocationProvider;
import org.travice.helper.Log;
import org.travice.model.Attribute;
import org.travice.model.BaseModel;
import org.travice.model.Calendar;
import org.travice.model.Command;
import org.travice.model.Device;
import org.travice.model.Driver;
import org.travice.model.Geofence;
import org.travice.model.Group;
import org.travice.model.Maintenance;
import org.travice.model.Notification;
import org.travice.model.User;
import org.travice.geolocation.GoogleGeolocationProvider;
import org.travice.geolocation.GeolocationProvider;
import org.travice.geolocation.MozillaGeolocationProvider;
import org.travice.geolocation.OpenCellIdGeolocationProvider;
import org.travice.notification.EventForwarder;
import org.travice.notification.JsonTypeEventForwarder;
import org.travice.notification.NotificatorManager;
import org.travice.reports.model.TripsConfig;
import org.travice.sms.SmsManager;
import org.travice.web.WebServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public final class Context {

    private Context() {
    }

    private static Config config;

    public static Config getConfig() {
        return config;
    }

    private static boolean loggerEnabled;

    public static boolean isLoggerEnabled() {
        return loggerEnabled;
    }

    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static IdentityManager identityManager;

    public static IdentityManager getIdentityManager() {
        return identityManager;
    }

    private static DataManager dataManager;

    public static DataManager getDataManager() {
        return dataManager;
    }

    private static LdapProvider ldapProvider;

    public static LdapProvider getLdapProvider() {
        return ldapProvider;
    }

    private static MediaManager mediaManager;

    public static MediaManager getMediaManager() {
        return mediaManager;
    }

    private static UsersManager usersManager;

    public static UsersManager getUsersManager() {
        return usersManager;
    }

    private static GroupsManager groupsManager;

    public static GroupsManager getGroupsManager() {
        return groupsManager;
    }

    private static DeviceManager deviceManager;

    public static DeviceManager getDeviceManager() {
        return deviceManager;
    }

    private static ConnectionManager connectionManager;

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private static PermissionsManager permissionsManager;

    public static PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    private static Geocoder geocoder;

    public static Geocoder getGeocoder() {
        return geocoder;
    }

    private static GeolocationProvider geolocationProvider;

    public static GeolocationProvider getGeolocationProvider() {
        return geolocationProvider;
    }

    private static WebServer webServer;

    public static WebServer getWebServer() {
        return webServer;
    }

    private static ServerManager serverManager;

    public static ServerManager getServerManager() {
        return serverManager;
    }

    private static GeofenceManager geofenceManager;

    public static GeofenceManager getGeofenceManager() {
        return geofenceManager;
    }

    private static CalendarManager calendarManager;

    public static CalendarManager getCalendarManager() {
        return calendarManager;
    }

    private static NotificationManager notificationManager;

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    private static NotificatorManager notificatorManager;

    public static NotificatorManager getNotificatorManager() {
        return notificatorManager;
    }

    private static VelocityEngine velocityEngine;

    public static VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    private static Client client = ClientBuilder.newClient();

    public static Client getClient() {
        return client;
    }

    private static EventForwarder eventForwarder;

    public static EventForwarder getEventForwarder() {
        return eventForwarder;
    }

    private static AttributesManager attributesManager;

    public static AttributesManager getAttributesManager() {
        return attributesManager;
    }

    private static DriversManager driversManager;

    public static DriversManager getDriversManager() {
        return driversManager;
    }

    private static CommandsManager commandsManager;

    public static CommandsManager getCommandsManager() {
        return commandsManager;
    }

    private static MaintenancesManager maintenancesManager;

    public static MaintenancesManager getMaintenancesManager() {
        return maintenancesManager;
    }

    private static StatisticsManager statisticsManager;

    public static StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    private static SmsManager smsManager;

    public static SmsManager getSmsManager() {
        return smsManager;
    }

    private static MotionEventHandler motionEventHandler;

    public static MotionEventHandler getMotionEventHandler() {
        return motionEventHandler;
    }

    private static OverspeedEventHandler overspeedEventHandler;

    public static OverspeedEventHandler getOverspeedEventHandler() {
        return overspeedEventHandler;
    }

    private static TripsConfig tripsConfig;

    public static TripsConfig getTripsConfig() {
        return tripsConfig;
    }

    public static TripsConfig initTripsConfig() {
        return new TripsConfig(
                config.getLong("report.trip.minimalTripDistance", 500),
                config.getLong("report.trip.minimalTripDuration", 300) * 1000,
                config.getLong("report.trip.minimalParkingDuration", 300) * 1000,
                config.getLong("report.trip.minimalNoDataDuration", 3600) * 1000,
                config.getBoolean("report.trip.useIgnition"),
                config.getBoolean("event.motion.processInvalidPositions"),
                config.getDouble("event.motion.speedThreshold", 0.01));
    }

    public static Geocoder initGeocoder() {
        String type = config.getString("geocoder.type", "google");
        String url = config.getString("geocoder.url");
        String key = config.getString("geocoder.key");
        String language = config.getString("geocoder.language");

        String formatString = config.getString("geocoder.format");
        AddressFormat addressFormat;
        if (formatString != null) {
            addressFormat = new AddressFormat(formatString);
        } else {
            addressFormat = new AddressFormat();
        }

        int cacheSize = config.getInteger("geocoder.cacheSize");
        switch (type) {
            case "nominatim":
                return new NominatimGeocoder(url, key, language, cacheSize, addressFormat);
            case "gisgraphy":
                return new GisgraphyGeocoder(url, cacheSize, addressFormat);
            case "mapquest":
                return new MapQuestGeocoder(url, key, cacheSize, addressFormat);
            case "opencage":
                return new OpenCageGeocoder(url, key, cacheSize, addressFormat);
            case "bingmaps":
                return new BingMapsGeocoder(url, key, cacheSize, addressFormat);
            case "factual":
                return new FactualGeocoder(url, key, cacheSize, addressFormat);
            case "geocodefarm":
                return new GeocodeFarmGeocoder(key, language, cacheSize, addressFormat);
            case "geocodexyz":
                return new GeocodeXyzGeocoder(key, cacheSize, addressFormat);
            default:
                return new GoogleGeocoder(key, language, cacheSize, addressFormat);
        }
    }

    public static void init(String[] arguments) throws Exception {

        config = new Config();
        if (arguments.length <= 0) {
            throw new RuntimeException("Configuration file is not provided");
        }

        config.load(arguments[0]);

        loggerEnabled = config.getBoolean("logger.enable");
        if (loggerEnabled) {
            Log.setupLogger(config);
        }

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR353Module());
        objectMapper.setConfig(
                objectMapper.getSerializationConfig().without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        if (Context.getConfig().getBoolean("mapper.prettyPrintedJson")) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        JacksonJsonProvider jsonProvider =
                new JacksonJaxbJsonProvider(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
        client = ClientBuilder.newClient().register(jsonProvider);


        if (config.hasKey("database.url")) {
            dataManager = new DataManager(config);
        }

        if (config.getBoolean("ldap.enable")) {
            ldapProvider = new LdapProvider(config);
        }

        mediaManager = new MediaManager(config.getString("media.path"));

        if (dataManager != null) {
            usersManager = new UsersManager(dataManager);
            groupsManager = new GroupsManager(dataManager);
            deviceManager = new DeviceManager(dataManager);
        }

        identityManager = deviceManager;

        if (config.getBoolean("geocoder.enable")) {
            geocoder = initGeocoder();
        }

        if (config.getBoolean("geolocation.enable")) {
            initGeolocationModule();
        }

        if (config.getBoolean("web.enable")) {
            webServer = new WebServer(config, dataManager.getDataSource());
        }

        permissionsManager = new PermissionsManager(dataManager, usersManager);

        connectionManager = new ConnectionManager();

        tripsConfig = initTripsConfig();

        if (config.getBoolean("sms.enable")) {
            final String smsManagerClass = config.getString("sms.manager.class", "org.travice.smpp.SmppClient");
            try {
                smsManager = (SmsManager) Class.forName(smsManagerClass).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                Log.warning("Error loading SMS Manager class : " + smsManagerClass, e);
            }
        }

        if (config.getBoolean("event.enable")) {
            initEventsModule();
        }

        serverManager = new ServerManager();

        if (config.getBoolean("event.forward.enable")) {
            eventForwarder = new JsonTypeEventForwarder();
        }

        attributesManager = new AttributesManager(dataManager);

        driversManager = new DriversManager(dataManager);

        commandsManager = new CommandsManager(dataManager, config.getBoolean("commands.queueing"));

        statisticsManager = new StatisticsManager();

    }

    private static void initGeolocationModule() {

        String type = config.getString("geolocation.type", "mozilla");
        String url = config.getString("geolocation.url");
        String key = config.getString("geolocation.key");

        switch (type) {
            case "google":
                geolocationProvider = new GoogleGeolocationProvider(key);
                break;
            case "opencellid":
                geolocationProvider = new OpenCellIdGeolocationProvider(key);
                break;
            case "unwired":
                geolocationProvider = new UnwiredGeolocationProvider(url, key);
                break;
            default:
                geolocationProvider = new MozillaGeolocationProvider(key);
                break;
        }
    }

    private static void initEventsModule() {

        geofenceManager = new GeofenceManager(dataManager);
        calendarManager = new CalendarManager(dataManager);
        maintenancesManager = new MaintenancesManager(dataManager);
        notificationManager = new NotificationManager(dataManager);
        notificatorManager = new NotificatorManager();
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("file.resource.loader.path",
                Context.getConfig().getString("templates.rootPath", "templates") + "/");
        velocityProperties.setProperty("runtime.log.logsystem.class",
                "org.apache.velocity.runtime.log.NullLogChute");

        String address;
        try {
            address = config.getString("web.address", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            address = "localhost";
        }

        String webUrl = URIUtil.newURI("http", address, config.getInteger("web.port", 8082), "", "");
        webUrl = Context.getConfig().getString("web.url", webUrl);
        velocityProperties.setProperty("web.url", webUrl);

        velocityEngine = new VelocityEngine();
        velocityEngine.init(velocityProperties);

        motionEventHandler = new MotionEventHandler(tripsConfig);
        overspeedEventHandler = new OverspeedEventHandler(
                Context.getConfig().getLong("event.overspeed.minimalDuration") * 1000,
                Context.getConfig().getBoolean("event.overspeed.notRepeat"),
                Context.getConfig().getBoolean("event.overspeed.preferLowest"));
    }

    public static void init(IdentityManager testIdentityManager) {
        config = new Config();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR353Module());
        JacksonJsonProvider jsonProvider =
                new JacksonJaxbJsonProvider(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
        client = ClientBuilder.newClient().register(jsonProvider);
        identityManager = testIdentityManager;
    }

    public static <T extends BaseModel> BaseObjectManager<T> getManager(Class<T> clazz) {
        if (clazz.equals(Device.class)) {
            return (BaseObjectManager<T>) deviceManager;
        } else if (clazz.equals(Group.class)) {
            return (BaseObjectManager<T>) groupsManager;
        } else if (clazz.equals(User.class)) {
            return (BaseObjectManager<T>) usersManager;
        } else if (clazz.equals(Calendar.class)) {
            return (BaseObjectManager<T>) calendarManager;
        } else if (clazz.equals(Attribute.class)) {
            return (BaseObjectManager<T>) attributesManager;
        } else if (clazz.equals(Geofence.class)) {
            return (BaseObjectManager<T>) geofenceManager;
        } else if (clazz.equals(Driver.class)) {
            return (BaseObjectManager<T>) driversManager;
        } else if (clazz.equals(Command.class)) {
            return (BaseObjectManager<T>) commandsManager;
        } else if (clazz.equals(Maintenance.class)) {
            return (BaseObjectManager<T>) maintenancesManager;
        } else if (clazz.equals(Notification.class)) {
            return (BaseObjectManager<T>) notificationManager;
        }
        return null;
    }

}
