
package org.travice.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.travice.database.DataManager;

public class Permission {

    private Class<?> ownerClass;
    private long ownerId;
    private Class<?> propertyClass;
    private long propertyId;

    public Permission(LinkedHashMap<String, Long> permissionMap) throws ClassNotFoundException {
        Iterator<Map.Entry<String, Long>> iterator = permissionMap.entrySet().iterator();
        String owner = iterator.next().getKey();
        ownerClass = DataManager.getClassByName(owner);
        String property = iterator.next().getKey();
        propertyClass = DataManager.getClassByName(property);
        ownerId = permissionMap.get(owner);
        propertyId = permissionMap.get(property);
    }

    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public Class<?> getPropertyClass() {
        return propertyClass;
    }

    public long getPropertyId() {
        return propertyId;
    }
}
