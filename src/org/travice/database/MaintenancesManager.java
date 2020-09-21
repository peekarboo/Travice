
package org.travice.database;

import org.travice.model.Maintenance;

public class MaintenancesManager extends ExtendedObjectManager<Maintenance> {

    public MaintenancesManager(DataManager dataManager) {
        super(dataManager, Maintenance.class);
    }

}
