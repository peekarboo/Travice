
package org.travice.reports;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.jxls.util.JxlsHelper;
import org.travice.Context;
import org.travice.model.Position;
import org.travice.reports.model.SummaryReport;

public final class Summary {

    private Summary() {
    }

    private static SummaryReport calculateSummaryResult(long deviceId, Date from, Date to) throws SQLException {
        SummaryReport result = new SummaryReport();
        result.setDeviceId(deviceId);
        result.setDeviceName(Context.getIdentityManager().getById(deviceId).getName());
        Collection<Position> positions = Context.getDataManager().getPositions(deviceId, from, to);
        if (positions != null && !positions.isEmpty()) {
            Position firstPosition = null;
            Position previousPosition = null;
            double speedSum = 0;
            boolean engineHoursEnabled = Context.getConfig().getBoolean("processing.engineHours.enable");
            for (Position position : positions) {
                if (firstPosition == null) {
                    firstPosition = position;
                }
                if (engineHoursEnabled && previousPosition != null
                        && position.getBoolean(Position.KEY_IGNITION)
                        && previousPosition.getBoolean(Position.KEY_IGNITION)) {
                    // Temporary fallback for old data, to be removed in May 2019
                    result.addEngineHours(position.getFixTime().getTime()
                            - previousPosition.getFixTime().getTime());
                }
                previousPosition = position;
                speedSum += position.getSpeed();
                result.setMaxSpeed(position.getSpeed());
            }
            boolean ignoreOdometer = Context.getDeviceManager()
                    .lookupAttributeBoolean(deviceId, "report.ignoreOdometer", false, true);
            result.setDistance(ReportUtils.calculateDistance(firstPosition, previousPosition, !ignoreOdometer));
            result.setAverageSpeed(speedSum / positions.size());
            result.setSpentFuel(ReportUtils.calculateFuel(firstPosition, previousPosition));

            if (engineHoursEnabled
                    && firstPosition.getAttributes().containsKey(Position.KEY_HOURS)
                    && previousPosition.getAttributes().containsKey(Position.KEY_HOURS)) {
                result.setEngineHours(
                        previousPosition.getLong(Position.KEY_HOURS) - firstPosition.getLong(Position.KEY_HOURS));
            }

            if (!ignoreOdometer
                    && firstPosition.getDouble(Position.KEY_ODOMETER) != 0
                    && previousPosition.getDouble(Position.KEY_ODOMETER) != 0) {
                result.setStartOdometer(firstPosition.getDouble(Position.KEY_ODOMETER));
                result.setEndOdometer(previousPosition.getDouble(Position.KEY_ODOMETER));
            } else {
                result.setStartOdometer(firstPosition.getDouble(Position.KEY_TOTAL_DISTANCE));
                result.setEndOdometer(previousPosition.getDouble(Position.KEY_TOTAL_DISTANCE));
            }

        }
        return result;
    }

    public static Collection<SummaryReport> getObjects(long userId, Collection<Long> deviceIds,
            Collection<Long> groupIds, Date from, Date to) throws SQLException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<SummaryReport> result = new ArrayList<>();
        for (long deviceId: ReportUtils.getDeviceList(deviceIds, groupIds)) {
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            result.add(calculateSummaryResult(deviceId, from, to));
        }
        return result;
    }

    public static void getExcel(OutputStream outputStream,
            long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) throws SQLException, IOException {
        ReportUtils.checkPeriodLimit(from, to);
        Collection<SummaryReport> summaries = getObjects(userId, deviceIds, groupIds, from, to);
        String templatePath = Context.getConfig().getString("report.templatesPath",
                "templates/export/");
        try (InputStream inputStream = new FileInputStream(templatePath + "/summary.xlsx")) {
            org.jxls.common.Context jxlsContext = ReportUtils.initializeContext(userId);
            jxlsContext.putVar("summaries", summaries);
            jxlsContext.putVar("from", from);
            jxlsContext.putVar("to", to);
            JxlsHelper.getInstance().setUseFastFormulaProcessor(false)
                    .processTemplate(inputStream, outputStream, jxlsContext);
        }
    }
}
