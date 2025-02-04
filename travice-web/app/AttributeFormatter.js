Ext.define('Travice.AttributeFormatter', {
    singleton: true,

    numberFormatterFactory: function (precision, suffix) {
        return function (value) {
            if (value !== undefined) {
                return Number(value.toFixed(precision)) + ' ' + suffix;
            }
            return null;
        };
    },

    coordinateFormatter: function (key, value) {
        return Ext.getStore('CoordinateFormats').formatValue(key, value, Travice.app.getPreference('coordinateFormat'));
    },

    speedFormatter: function (value) {
        return Ext.getStore('SpeedUnits').formatValue(value, Travice.app.getAttributePreference('speedUnit'));
    },

    speedConverter: function (value) {
        return Ext.getStore('SpeedUnits').convertValue(value, Travice.app.getAttributePreference('speedUnit'));
    },

    courseFormatter: function (value) {
        var courseValues = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
        return courseValues[Math.floor(value / 45)];
    },

    distanceFormatter: function (value) {
        return Ext.getStore('DistanceUnits').formatValue(value, Travice.app.getAttributePreference('distanceUnit'));
    },

    distanceConverter: function (value) {
        return Ext.getStore('DistanceUnits').convertValue(value, Travice.app.getAttributePreference('distanceUnit'));
    },

    volumeFormatter: function (value) {
        return Ext.getStore('VolumeUnits').formatValue(value, Travice.app.getAttributePreference('volumeUnit'));
    },

    volumeConverter: function (value) {
        return Ext.getStore('VolumeUnits').convertValue(value, Travice.app.getAttributePreference('volumeUnit'));
    },

    hoursFormatter: function (value) {
        return Ext.getStore('HoursUnits').formatValue(value, 'h');
    },

    hoursConverter: function (value) {
        return Ext.getStore('HoursUnits').convertValue(value, 'h');
    },

    durationFormatter: function (value) {
        return Ext.getStore('HoursUnits').formatValue(value, 'h', true);
    },

    deviceIdFormatter: function (value) {
        var device, store;
        if (value !== 0) {
            store = Ext.getStore('AllDevices');
            if (store.getTotalCount() === 0) {
                store = Ext.getStore('Devices');
            }
            device = store.getById(value);
            return device ? device.get('name') : '';
        }
        return null;
    },

    groupIdFormatter: function (value) {
        var group, store;
        if (value !== 0) {
            store = Ext.getStore('AllGroups');
            if (store.getTotalCount() === 0) {
                store = Ext.getStore('Groups');
            }
            group = store.getById(value);
            return group ? group.get('name') : value;
        }
        return null;
    },

    geofenceIdFormatter: function (value) {
        var geofence, store;
        if (value !== 0) {
            store = Ext.getStore('AllGeofences');
            if (store.getTotalCount() === 0) {
                store = Ext.getStore('Geofences');
            }
            geofence = store.getById(value);
            return geofence ? geofence.get('name') : '';
        }
        return null;
    },

    calendarIdFormatter: function (value) {
        var calendar, store;
        if (value !== 0) {
            store = Ext.getStore('AllCalendars');
            if (store.getTotalCount() === 0) {
                store = Ext.getStore('Calendars');
            }
            calendar = store.getById(value);
            return calendar ? calendar.get('name') : '';
        }
        return null;
    },

    driverUniqueIdFormatter: function (value) {
        var driver, store;
        if (value !== 0) {
            store = Ext.getStore('AllDrivers');
            if (store.getTotalCount() === 0) {
                store = Ext.getStore('Drivers');
            }
            driver = store.findRecord('uniqueId', value, 0, false, true, true);
            return driver ? value + ' (' + driver.get('name') + ')' : value;
        }
        return null;
    },

    maintenanceIdFormatter: function (value) {
        var maintenance, store;
        if (value !== 0) {
            store = Ext.getStore('AllMaintenances');
            if (store.getTotalCount() === 0) {
                store = Ext.getStore('Maintenances');
            }
            maintenance = store.getById(value);
            return maintenance ? maintenance.get('name') : '';
        }
        return null;
    },

    lastUpdateFormatter: function (value) {
        var seconds, interval;
        if (value) {
            seconds = Math.floor((new Date() - value) / 1000);
            if (seconds < 0) {
                seconds = 0;
            }
            interval = Math.floor(seconds / 86400);
            if (interval > 1) {
                return interval + ' ' + Strings.sharedDays;
            }
            interval = Math.floor(seconds / 3600);
            if (interval > 1) {
                return interval + ' ' + Strings.sharedHours;
            }
            return Math.floor(seconds / 60) + ' ' + Strings.sharedMinutes;
        }
        return null;
    },

    commandTypeFormatter: function (value) {
        var name = Strings['command' + value.charAt(0).toUpperCase() + value.slice(1)];
        return name ? name : value;
    },

    defaultFormatter: function (value) {
        if (typeof value === 'number') {
            return Number(value.toFixed(Travice.Style.numberPrecision));
        } else if (typeof value === 'boolean') {
            return value ? Ext.Msg.buttonText.yes : Ext.Msg.buttonText.no;
        } else if (value instanceof Date) {
            if (Travice.app.getPreference('twelveHourFormat', false)) {
                return Ext.Date.format(value, Travice.Style.dateTimeFormat12);
            } else {
                return Ext.Date.format(value, Travice.Style.dateTimeFormat24);
            }
        }
        return value;
    },

    getFormatter: function (key) {
        var self = this;

        switch (key) {
            case 'latitude':
            case 'longitude':
                return function (value) {
                    return self.coordinateFormatter(key, value);
                };
            case 'speed':
                return this.speedFormatter;
            case 'course':
                return this.courseFormatter;
            case 'distance':
            case 'accuracy':
                return this.distanceFormatter;
            case 'duration':
                return this.durationFormatter;
            case 'deviceId':
                return this.deviceIdFormatter;
            case 'groupId':
                return this.groupIdFormatter;
            case 'geofenceId':
                return this.geofenceIdFormatter;
            case 'maintenanceId':
                return this.maintenanceIdFormatter;
            case 'calendarId':
                return this.calendarIdFormatter;
            case 'lastUpdate':
                return this.lastUpdateFormatter;
            case 'spentFuel':
                return this.volumeFormatter;
            case 'driverUniqueId':
                return this.driverUniqueIdFormatter;
            case 'commandType':
                return this.commandTypeFormatter;
            default:
                return this.defaultFormatter;
        }
    },

    getConverter: function (key) {
        switch (key) {
            case 'speed':
                return this.speedConverter;
            case 'distance':
            case 'accuracy':
                return this.distanceConverter;
            case 'spentFuel':
                return this.volumeConverter;
            default:
                return function (value) {
                    return value;
                };
        }
    },

    getAttributeFormatter: function (key) {
        var dataType = Ext.getStore('PositionAttributes').getAttributeDataType(key);

        switch (dataType) {
            case 'distance':
                return this.distanceFormatter;
            case 'speed':
                return this.speedFormatter;
            case 'driverUniqueId':
                return this.driverUniqueIdFormatter;
            case 'voltage':
                return this.numberFormatterFactory(Travice.Style.numberPrecision, Strings.sharedVoltAbbreviation);
            case 'percentage':
                return this.numberFormatterFactory(Travice.Style.numberPrecision, '&#37;');
            case 'temperature':
                return this.numberFormatterFactory(Travice.Style.numberPrecision, '&deg;C');
            case 'volume':
                return this.volumeFormatter;
            case 'hours':
                return this.hoursFormatter;
            case 'consumption':
                return this.numberFormatterFactory(Travice.Style.numberPrecision, Strings.sharedLiterPerHourAbbreviation);
            default:
                return this.defaultFormatter;
        }
    },

    getAttributeConverter: function (key) {
        var dataType = Ext.getStore('PositionAttributes').getAttributeDataType(key);

        switch (dataType) {
            case 'distance':
                return this.distanceConverter;
            case 'speed':
                return this.speedConverter;
            case 'volume':
                return this.volumeConverter;
            case 'hours':
                return this.hoursConverter;
            default:
                return function (value) {
                    return value;
                };
        }
    },

    renderAttribute: function (value, attribute) {
        if (attribute && attribute.get('dataType') === 'speed') {
            return Ext.getStore('SpeedUnits').formatValue(value, Travice.app.getAttributePreference('speedUnit', 'kn'), true);
        } else if (attribute && attribute.get('dataType') === 'distance') {
            return Ext.getStore('DistanceUnits').formatValue(value, Travice.app.getAttributePreference('distanceUnit', 'km'), true);
        } else if (attribute && attribute.get('dataType') === 'hours') {
            return Ext.getStore('HoursUnits').formatValue(value, 'h', true);
        } else {
            return value;
        }
    }
});
