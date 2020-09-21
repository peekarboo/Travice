
package org.travice.helper;

import java.beans.Introspector;

import org.travice.model.BaseModel;

public final class LogAction {

    private LogAction() {
    }

    private static final String ACTION_CREATE = "create";
    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_REMOVE = "remove";

    private static final String ACTION_LINK = "link";
    private static final String ACTION_UNLINK = "unlink";

    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_LOGOUT = "logout";

    private static final String ACTION_TOTAL_DISTANCE = "resetTotalDistance";

    private static final String PATTERN_OBJECT = "user: %d, action: %s, object: %s, id: %d";
    private static final String PATTERN_LINK = "user: %d, action: %s, owner: %s, id: %d, property: %s, id: %d";
    private static final String PATTERN_LOGIN = "user: %d, action: %s";
    private static final String PATTERN_TOTAL_DISTANCE = "user: %d, action: %s, deviceId: %d";

    public static void create(long userId, BaseModel object) {
        logObjectAction(ACTION_CREATE, userId, object.getClass(), object.getId());
    }

    public static void edit(long userId, BaseModel object) {
        logObjectAction(ACTION_EDIT, userId, object.getClass(), object.getId());
    }

    public static void remove(long userId, Class<?> clazz, long objectId) {
        logObjectAction(ACTION_REMOVE, userId, clazz, objectId);
    }

    public static void link(long userId, Class<?> owner, long ownerId, Class<?> property, long propertyId) {
        logLinkAction(ACTION_LINK, userId, owner, ownerId, property, propertyId);
    }

    public static void unlink(long userId, Class<?> owner, long ownerId, Class<?> property, long propertyId) {
        logLinkAction(ACTION_UNLINK, userId, owner, ownerId, property, propertyId);
    }

    public static void login(long userId) {
        logLoginAction(ACTION_LOGIN, userId);
    }

    public static void logout(long userId) {
        logLoginAction(ACTION_LOGOUT, userId);
    }

    public static void resetTotalDistance(long userId, long deviceId) {
        log(String.format(PATTERN_TOTAL_DISTANCE, userId, ACTION_TOTAL_DISTANCE, deviceId));
    }

    private static void logObjectAction(String action, long userId, Class<?> clazz, long objectId) {
        log(String.format(PATTERN_OBJECT, userId, action, Introspector.decapitalize(clazz.getSimpleName()), objectId));
    }

    private static void logLinkAction(String action, long userId,
            Class<?> owner, long ownerId, Class<?> property, long propertyId) {
        log(String.format(PATTERN_LINK, userId, action,
                Introspector.decapitalize(owner.getSimpleName()), ownerId,
                Introspector.decapitalize(property.getSimpleName()), propertyId));
    }

    private static void logLoginAction(String action, long userId) {
        log(String.format(PATTERN_LOGIN, userId, action));
    }

    private static void log(String msg) {
        Log.info(msg);
    }

}
