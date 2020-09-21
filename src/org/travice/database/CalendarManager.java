
package org.travice.database;

import org.travice.model.Calendar;

public class CalendarManager extends SimpleObjectManager<Calendar> {

    public CalendarManager(DataManager dataManager) {
        super(dataManager, Calendar.class);
    }

}
