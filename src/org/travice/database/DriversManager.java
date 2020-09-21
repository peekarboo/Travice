
package org.travice.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.travice.model.Driver;

public class DriversManager extends ExtendedObjectManager<Driver> {

    private Map<String, Driver> driversByUniqueId;

    public DriversManager(DataManager dataManager) {
        super(dataManager, Driver.class);
        if (driversByUniqueId == null) {
            driversByUniqueId = new ConcurrentHashMap<>();
        }
    }

    private void putUniqueDriverId(Driver driver) {
        if (driversByUniqueId == null) {
            driversByUniqueId = new ConcurrentHashMap<>(getAllItems().size());
        }
        driversByUniqueId.put(driver.getUniqueId(), driver);
    }

    @Override
    protected void addNewItem(Driver driver) {
        super.addNewItem(driver);
        putUniqueDriverId(driver);
    }

    @Override
    protected void updateCachedItem(Driver driver) {
        Driver cachedDriver = getById(driver.getId());
        cachedDriver.setName(driver.getName());
        if (!driver.getUniqueId().equals(cachedDriver.getUniqueId())) {
            driversByUniqueId.remove(cachedDriver.getUniqueId());
            cachedDriver.setUniqueId(driver.getUniqueId());
            putUniqueDriverId(cachedDriver);
        }
        cachedDriver.setAttributes(driver.getAttributes());
    }

    @Override
    protected void removeCachedItem(long driverId) {
        Driver cachedDriver = getById(driverId);
        if (cachedDriver != null) {
            String driverUniqueId = cachedDriver.getUniqueId();
            super.removeCachedItem(driverId);
            driversByUniqueId.remove(driverUniqueId);
        }
    }

    public Driver getDriverByUniqueId(String uniqueId) {
        return driversByUniqueId.get(uniqueId);
    }
}
