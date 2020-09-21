
package org.travice.notification;

import org.travice.Config;
import org.travice.model.ExtendedModel;

public class PropertiesProvider {

    private Config config;

    private ExtendedModel extendedModel;

    public PropertiesProvider(Config config) {
        this.config = config;
    }

    public PropertiesProvider(ExtendedModel extendedModel) {
        this.extendedModel = extendedModel;
    }

    public String getString(String key) {
        if (config != null) {
            return config.getString(key);
        } else {
            return extendedModel.getString(key);
        }
    }

    public String getString(String key, String defaultValue) {
        String value = getString(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public int getInteger(String key, int defaultValue) {
        if (config != null) {
            return config.getInteger(key, defaultValue);
        } else {
            Object result = extendedModel.getAttributes().get(key);
            if (result != null) {
                return result instanceof String ? Integer.parseInt((String) result) : (Integer) result;
            } else {
                return defaultValue;
            }
        }
    }

    public Boolean getBoolean(String key) {
        if (config != null) {
            if (config.hasKey(key)) {
                return config.getBoolean(key);
            } else {
                return null;
            }
        } else {
            Object result = extendedModel.getAttributes().get(key);
            if (result != null) {
                return result instanceof String ? Boolean.valueOf((String) result) : (Boolean) result;
            } else {
                return null;
            }
        }
    }

}
