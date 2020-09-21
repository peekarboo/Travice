

Ext.define('Travice.view.DeviceMenuController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.deviceMenu',

    requires: [
        'Travice.view.permissions.Geofences',
        'Travice.view.permissions.Drivers',
        'Travice.view.permissions.Notifications',
        'Travice.view.edit.ComputedAttributes',
        'Travice.view.permissions.SavedCommands',
        'Travice.view.permissions.Maintenances',
        'Travice.view.BaseWindow'
    ],

    init: function () {
        this.lookupReference('menuDriversButton').setHidden(
            Travice.app.getVehicleFeaturesDisabled() || Travice.app.getBooleanAttributePreference('ui.disableDrivers'));
        this.lookupReference('menuComputedAttributesButton').setHidden(
            Travice.app.getBooleanAttributePreference('ui.disableComputedAttributes'));
        this.lookupReference('menuCommandsButton').setHidden(Travice.app.getPreference('limitCommands', false));
        this.lookupReference('menuDeviceDistanceButton').setHidden(
            !Travice.app.getUser().get('administrator') && Travice.app.getUser().get('userLimit') === 0 || Travice.app.getVehicleFeaturesDisabled());
        this.lookupReference('menuMaintenancesButton').setHidden(
            Travice.app.getVehicleFeaturesDisabled() || Travice.app.getBooleanAttributePreference('ui.disableMaintenances'));
    },

    onGeofencesClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedGeofences,
            items: {
                xtype: 'linkGeofencesView',
                baseObjectName: 'deviceId',
                linkObjectName: 'geofenceId',
                storeName: 'Geofences',
                baseObject: this.getView().up('deviceMenu').device.getId()
            }
        }).show();
    },

    onNotificationsClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedNotifications,
            items: {
                xtype: 'linkNotificationsView',
                baseObjectName: 'deviceId',
                linkObjectName: 'notificationId',
                storeName: 'Notifications',
                baseObject: this.getView().up('deviceMenu').device.getId()
            }
        }).show();
    },

    onComputedAttributesClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedComputedAttributes,
            items: {
                xtype: 'linkComputedAttributesView',
                baseObjectName: 'deviceId',
                linkObjectName: 'attributeId',
                storeName: 'ComputedAttributes',
                baseObject: this.getView().up('deviceMenu').device.getId()
            }
        }).show();
    },

    onDriversClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedDrivers,
            items: {
                xtype: 'linkDriversView',
                baseObjectName: 'deviceId',
                linkObjectName: 'driverId',
                storeName: 'Drivers',
                baseObject: this.getView().up('deviceMenu').device.getId()
            }
        }).show();
    },

    onCommandsClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedSavedCommands,
            items: {
                xtype: 'linkSavedCommandsView',
                baseObjectName: 'deviceId',
                linkObjectName: 'commandId',
                storeName: 'Commands',
                baseObject: this.getView().up('deviceMenu').device.getId()
            }
        }).show();
    },

    onMaintenancesClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedMaintenances,
            items: {
                xtype: 'linkMaintenancesView',
                baseObjectName: 'deviceId',
                linkObjectName: 'maintenanceId',
                storeName: 'Maintenances',
                baseObject: this.getView().up('deviceMenu').device.getId()
            }
        }).show();
    },

    onDeviceDistanceClick: function () {
        var position, dialog = Ext.create('Travice.view.dialog.DeviceDistance');
        dialog.deviceId = this.getView().up('deviceMenu').device.getId();
        position = Ext.getStore('LatestPositions').findRecord('deviceId', dialog.deviceId, 0, false, false, true);
        if (position) {
            dialog.lookupReference('totalDistance').setValue(position.get('attributes').totalDistance);
        }
        dialog.show();
    }
});
