Ext.define('Travice.Application', {
    extend: 'Ext.app.Application',
    name: 'Travice',

    requires: [
        'Travice.Style',
        'Travice.AttributeFormatter'
    ],

    models: [
        'Server',
        'User',
        'Group',
        'Device',
        'Position',
        'Attribute',
        'Command',
        'Event',
        'Geofence',
        'Notification',
        'ReportSummary',
        'ReportTrip',
        'ReportStop',
        'Calendar',
        'KnownAttribute',
        'Driver',
        'KnownCommand',
        'KnownNotification',
        'Maintenance'
    ],

    stores: [
        'Groups',
        'Devices',
        'AllGroups',
        'AllDevices',
        'Positions',
        'LatestPositions',
        'EventPositions',
        'Users',
        'Attributes',
        'MapTypes',
        'DistanceUnits',
        'SpeedUnits',
        'CoordinateFormats',
        'CommandTypes',
        'TimeUnits',
        'Languages',
        'Events',
        'Geofences',
        'AllGeofences',
        'Notifications',
        'AllNotifications',
        'GeofenceTypes',
        'ReportRoute',
        'ReportEvents',
        'ReportTrips',
        'ReportStops',
        'ReportSummary',
        'ReportTypes',
        'ReportEventTypes',
        'ReportChartTypes',
        'ReportPeriods',
        'Statistics',
        'DeviceImages',
        'Calendars',
        'AllCalendars',
        'AllTimezones',
        'VisibleDevices',
        'DeviceStatuses',
        'CommonDeviceAttributes',
        'DeviceAttributes',
        'GeofenceAttributes',
        'GroupAttributes',
        'ServerAttributes',
        'CommonUserAttributes',
        'UserAttributes',
        'ComputedAttributes',
        'AllComputedAttributes',
        'PositionAttributes',
        'AttributeValueTypes',
        'Drivers',
        'AllDrivers',
        'KnownCommands',
        'VolumeUnits',
        'AllCommandTypes',
        'Commands',
        'AllCommands',
        'DeviceCommands',
        'AllNotificationTypes',
        'Maintenances',
        'AllMaintenances',
        'MaintenanceTypes',
        'HoursUnits',
        'AllNotificators'
    ],

    controllers: [
        'Root'
    ],

    isMobile: function () {
        return window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    },

    getVehicleFeaturesDisabled: function () {
        return this.getBooleanAttributePreference('ui.disableVehicleFetures');
    },

    getEventString: function (eventType) {
        var key = 'event' + eventType.charAt(0).toUpperCase() + eventType.slice(1);
        return Strings[key] || key;
    },

    getNotificatorString: function (eventType) {
        var key = 'notificator' + eventType.charAt(0).toUpperCase() + eventType.slice(1);
        return Strings[key] || key;
    },

    showReports: function (show) {
        var rootPanel = Ext.getCmp('rootPanel');
        if (rootPanel) {
            rootPanel.setActiveItem(show ? 1 : 0);
        }
    },

    showEvents: function (show) {
        var rootPanel = Ext.getCmp('rootPanel');
        if (rootPanel) {
            rootPanel.setActiveItem(show ? 2 : 0);
        }
    },

    setUser: function (data) {
        var reader = Ext.create('Ext.data.reader.Json', {
            model: 'Travice.model.User'
        });
        this.user = reader.readRecords(data).getRecords()[0];
    },

    getUser: function () {
        return this.user;
    },

    setServer: function (data) {
        var reader = Ext.create('Ext.data.reader.Json', {
            model: 'Travice.model.Server'
        });
        this.server = reader.readRecords(data).getRecords()[0];
    },

    getServer: function () {
        return this.server;
    },

    getPreference: function (key, defaultValue) {
        if (this.getServer().get('forceSettings')) {
            return this.getServer().get(key) || this.getUser().get(key) || defaultValue;
        } else {
            return this.getUser().get(key) || this.getServer().get(key) || defaultValue;
        }
    },

    getAttributePreference: function (key, defaultValue) {
        if (this.getServer().get('forceSettings')) {
            return this.getServer().get('attributes')[key] || this.getUser().get('attributes')[key] || defaultValue;
        } else {
            return this.getUser().get('attributes')[key] || this.getServer().get('attributes')[key] || defaultValue;
        }
    },

    getBooleanAttributePreference: function (key) {
        return this.getAttributePreference(key, false).toString() === 'true';
    },

    getReportColor: function (deviceId) {
        var index, reportColor, device = Ext.getStore('Devices').getById(deviceId);
        if (device) {
            reportColor = device.get('attributes')['web.reportColor'];
        }
        if (reportColor) {
            return reportColor;
        } else {
            index = 0;
            if (deviceId !== undefined) {
                index = deviceId % Travice.Style.mapRouteColor.length;
            }
            return Travice.Style.mapRouteColor[index];
        }
    },

    showError: function (error) {
        if (Ext.isString(error)) {
            Ext.Msg.alert(Strings.errorTitle, error);
        } else if (error.responseText) {
            Ext.Msg.alert(Strings.errorTitle, Strings.errorGeneral +
                    '<br><br><textarea readonly rows="5" style="resize: none; width: 100%;">' +
                    error.responseText + '</textarea>');
        } else if (error.statusText) {
            Ext.Msg.alert(Strings.errorTitle, error.statusText);
        } else {
            Ext.Msg.alert(Strings.errorTitle, Strings.errorConnection);
        }
    },

    showToast: function (message, title) {
        Ext.toast({
            html: message,
            title: title,
            width: Travice.Style.toastWidth,
            align: 'br'
        });
    }
});
