

Ext.define('Travice.view.SettingsMenuController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.settings',

    requires: [
        'Travice.view.dialog.LoginController',
        'Travice.view.dialog.User',
        'Travice.view.dialog.Server',
        'Travice.view.edit.Users',
        'Travice.view.edit.Groups',
        'Travice.view.edit.Geofences',
        'Travice.view.edit.Drivers',
        'Travice.view.edit.Notifications',
        'Travice.view.edit.ComputedAttributes',
        'Travice.view.Statistics',
        'Travice.view.dialog.DeviceDistance',
        'Travice.view.edit.Calendars',
        'Travice.view.edit.SavedCommands',
        'Travice.view.edit.Maintenances',
        'Travice.view.BaseWindow'
    ],

    init: function () {
        var admin, manager, readonly, deviceReadonly;
        admin = Travice.app.getUser().get('administrator');
        manager = Travice.app.getUser().get('userLimit') !== 0;
        readonly = Travice.app.getPreference('readonly', false);
        deviceReadonly = Travice.app.getUser().get('deviceReadonly');
        if (admin) {
            this.lookupReference('settingsServerButton').setHidden(false);
            this.lookupReference('settingsStatisticsButton').setHidden(false);
        }
        if (admin || manager) {
            this.lookupReference('settingsUsersButton').setHidden(false);
        }
        if (admin || !readonly) {
            this.lookupReference('settingsUserButton').setHidden(false);
            this.lookupReference('settingsGroupsButton').setHidden(false);
            this.lookupReference('settingsGeofencesButton').setHidden(false);
            this.lookupReference('settingsNotificationsButton').setHidden(false);
            this.lookupReference('settingsCalendarsButton').setHidden(
                Travice.app.getBooleanAttributePreference('ui.disableCalendars'));
            this.lookupReference('settingsDriversButton').setHidden(
                Travice.app.getVehicleFeaturesDisabled() || Travice.app.getBooleanAttributePreference('ui.disableDrivers'));
            this.lookupReference('settingsCommandsButton').setHidden(Travice.app.getPreference('limitCommands', false));
            this.lookupReference('settingsMaintenancesButton').setHidden(
                Travice.app.getVehicleFeaturesDisabled() || Travice.app.getBooleanAttributePreference('ui.disableMaintenances'));
        }
        if (admin || !deviceReadonly && !readonly) {
            this.lookupReference('settingsComputedAttributesButton').setHidden(
                Travice.app.getBooleanAttributePreference('ui.disableComputedAttributes'));
        }
    },

    onUserClick: function () {
        var dialog = Ext.create('Travice.view.dialog.User', {
            selfEdit: true
        });
        dialog.down('form').loadRecord(Travice.app.getUser());
        dialog.lookupReference('testNotificationButton').setHidden(false);
        dialog.show();
    },

    onGroupsClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.settingsGroups,
            items: {
                xtype: 'groupsView'
            }
        }).show();
    },

    onGeofencesClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedGeofences,
            items: {
                xtype: 'geofencesView'
            }
        }).show();
    },

    onServerClick: function () {
        var dialog = Ext.create('Travice.view.dialog.Server');
        dialog.down('form').loadRecord(Travice.app.getServer());
        dialog.show();
    },

    onUsersClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.settingsUsers,
            items: {
                xtype: 'usersView'
            }
        }).show();
    },

    onNotificationsClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedNotifications,
            items: {
                xtype: 'notificationsView'
            }
        }).show();
    },

    onComputedAttributesClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedComputedAttributes,
            items: {
                xtype: 'computedAttributesView'
            }
        }).show();
    },

    onStatisticsClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.statisticsTitle,
            items: {
                xtype: 'statisticsView'
            }
        }).show();
    },

    onCalendarsClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedCalendars,
            items: {
                xtype: 'calendarsView'
            }
        }).show();
    },

    onDriversClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedDrivers,
            items: {
                xtype: 'driversView'
            }
        }).show();
    },

    onCommandsClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedSavedCommands,
            items: {
                xtype: 'savedCommandsView'
            }
        }).show();
    },

    onMaintenancesClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedMaintenances,
            items: {
                xtype: 'maintenancesView'
            }
        }).show();
    },

    onLogoutClick: function () {
        Ext.create('Travice.view.dialog.LoginController').logout();
    }
});
