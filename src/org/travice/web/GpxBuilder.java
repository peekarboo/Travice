
package org.travice.web;

import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.travice.helper.UnitsConverter;
import org.travice.model.Position;

public class GpxBuilder {

    private StringBuilder builder = new StringBuilder();
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>"
            + "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"Travice\" version=\"1.1\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 "
            + "http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
    private static final String NAME = "<name>%1$s</name><trkseg>%n";
    private static final String POINT = "<trkpt lat=\"%1$f\" lon=\"%2$f\">"
            + "<time>%3$s</time>"
            + "<geoidheight>%4$f</geoidheight>"
            + "<course>%5$f</course>"
            + "<speed>%6$f</speed>"
            + "</trkpt>%n";
    private static final String FOOTER = "</trkseg></trk></gpx>";

    private static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime();

    public GpxBuilder() {
        builder.append(HEADER);
        builder.append("<trkseg>\n");
    }

    public GpxBuilder(String name) {
        builder.append(HEADER);
        builder.append(String.format(NAME, name));
    }

    public void addPosition(Position position) {
        builder.append(String.format(POINT, position.getLatitude(), position.getLongitude(),
                DATE_FORMAT.print(new DateTime(position.getFixTime())), position.getAltitude(),
                position.getCourse(), UnitsConverter.mpsFromKnots(position.getSpeed())));
    }

    public void addPositions(Collection<Position> positions) {
        for (Position position : positions) {
            addPosition(position);
        }
    }

    public String build() {
        builder.append(FOOTER);
        return builder.toString();
    }

}
