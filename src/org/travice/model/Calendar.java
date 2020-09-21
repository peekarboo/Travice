
package org.travice.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.CalendarComponent;
import org.apache.commons.collections4.Predicate;
import org.travice.database.QueryIgnore;

public class Calendar extends ExtendedModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private byte[] data;

    public byte[] getData() {
        return data.clone();
    }

    public void setData(byte[] data) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        calendar = builder.build(new ByteArrayInputStream(data));
        this.data = data.clone();
    }

    private net.fortuna.ical4j.model.Calendar calendar;

    @QueryIgnore
    @JsonIgnore
    public net.fortuna.ical4j.model.Calendar getCalendar() {
        return calendar;
    }

    public boolean checkMoment(Date date) {
        if (calendar != null) {
            Period period = new Period(new DateTime(date), new Dur(0, 0, 0, 0));
            Predicate<CalendarComponent> periodRule = new PeriodRule<>(period);
            Filter<CalendarComponent> filter = new Filter<>(new Predicate[] {periodRule}, Filter.MATCH_ANY);
            Collection<CalendarComponent> events = filter.filter(calendar.getComponents(CalendarComponent.VEVENT));
            if (events != null && !events.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
