

Ext.define('Travice.view.edit.UsersController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.users',

    requires: [
        'Travice.view.dialog.User',
        'Travice.view.permissions.Devices',
        'Travice.view.permissions.Groups',
        'Travice.view.permissions.Geofences',
        'Travice.view.permissions.Calendars',
        'Travice.view.permissions.Users',
        'Travice.view.permissions.ComputedAttributes',
        'Travice.view.permissions.Drivers',
        'Travice.view.permissions.SavedCommands',
        'Travice.view.permissions.Notifications',
        'Travice.view.permissions.Maintenances',
        'Travice.view.BaseWindow',
        'Travice.model.User'
    ],

    objectModel: 'Travice.model.User',
    objectDialog: 'Travice.view.dialog.User',
    removeTitle: Strings.settingsUser,

    init: function () {
        Ext.getStore('Users').load();
        this.lookupReference('userUsersButton').setHidden(!Travice.app.getUser().get('administrator'));
        this.lookupReference('userDriversButton').setHidden(
            Travice.app.getVehicleFeaturesDisabled() || Travice.app.getBooleanAttributePreference('ui.disableDrivers'));
        this.lookupReference('userAttributesButton').setHidden(
            Travice.app.getBooleanAttributePreference('ui.disableComputedAttributes'));
        this.lookupReference('userCalendarsButton').setHidden(
            Travice.app.getBooleanAttributePreference('ui.disableCalendars'));
        this.lookupReference('userCommandsButton').setHidden(Travice.app.getPreference('limitCommands', false));
        this.lookupReference('userMaintenancesButton').setHidden(
            Travice.app.getVehicleFeaturesDisabled() || Travice.app.getBooleanAttributePreference('ui.disableMaintenances'));
    },

    onEditClick: function () {
        var dialog, user = this.getView().getSelectionModel().getSelection()[0];
        dialog = Ext.create('Travice.view.dialog.User', {
            selfEdit: user.get('id') === Travice.app.getUser().get('id')
        });
        dialog.down('form').loadRecord(user);
        dialog.show();
    },

    onAddClick: function () {
        var user, dialog;
        user = Ext.create('Travice.model.User');
        if (Travice.app.getUser().get('administrator')) {
            user.set('deviceLimit', -1);
        }
        if (Travice.app.getUser().get('expirationTime')) {
            user.set('expirationTime', Travice.app.getUser().get('expirationTime'));
        }
        dialog = Ext.create('Travice.view.dialog.User');
        dialog.down('form').loadRecord(user);
        dialog.show();
    },

    onDevicesClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.getStore('AllGroups').load();
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.deviceTitle,
            items: {
                xtype: 'linkDevicesView',
                baseObjectName: 'userId',
                linkObjectName: 'deviceId',
                storeName: 'AllDevices',
                linkStoreName: 'Devices',
                baseObject: user.getId()
            }
        }).show();
    },

    onGroupsClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.settingsGroups,
            items: {
                xtype: 'linkGroupsView',
                baseObjectName: 'userId',
                linkObjectName: 'groupId',
                storeName: 'AllGroups',
                linkStoreName: 'Groups',
                baseObject: user.getId()
            }
        }).show();
    },

    onGeofencesClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedGeofences,
            items: {
                xtype: 'linkGeofencesView',
                baseObjectName: 'userId',
                linkObjectName: 'geofenceId',
                storeName: 'AllGeofences',
                linkStoreName: 'Geofences',
                baseObject: user.getId()
            }
        }).show();
    },

    onNotificationsClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedNotifications,
            items: {
                xtype: 'linkNotificationsView',
                baseObjectName: 'userId',
                linkObjectName: 'notificationId',
                storeName: 'AllNotifications',
                linkStoreName: 'Notifications',
                baseObject: user.getId()
            }
        }).show();
    },

    onCalendarsClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedCalendars,
            items: {
                xtype: 'linkCalendarsView',
                baseObjectName: 'userId',
                linkObjectName: 'calendarId',
                storeName: 'AllCalendars',
                linkStoreName: 'Calendars',
                baseObject: user.getId()
            }
        }).show();
    },

    onUsersClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.settingsUsers,
            items: {
                xtype: 'linkUsersView',
                baseObjectName: 'userId',
                linkObjectName: 'managedUserId',
                storeName: 'Users',
                baseObject: user.getId()
            }
        }).show();
    },

    onAttributesClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedComputedAttributes,
            items: {
                xtype: 'linkComputedAttributesView',
                baseObjectName: 'userId',
                linkObjectName: 'attributeId',
                storeName: 'AllComputedAttributes',
                linkStoreName: 'ComputedAttributes',
                baseObject: user.getId()
            }
        }).show();
    },

    onDriversClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedDrivers,
            items: {
                xtype: 'linkDriversView',
                baseObjectName: 'userId',
                linkObjectName: 'driverId',
                storeName: 'AllDrivers',
                linkStoreName: 'Drivers',
                baseObject: user.getId()
            }
        }).show();
    },

    onCommandsClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedSavedCommands,
            items: {
                xtype: 'linkSavedCommandsView',
                baseObjectName: 'userId',
                linkObjectName: 'commandId',
                storeName: 'AllCommands',
                linkStoreName: 'Commands',
                baseObject: user.getId()
            }
        }).show();
    },

    onMaintenancesClick: function () {
        var user = this.getView().getSelectionModel().getSelection()[0];
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedMaintenances,
            items: {
                xtype: 'linkMaintenancesView',
                baseObjectName: 'userId',
                linkObjectName: 'maintenanceId',
                storeName: 'AllMaintenances',
                linkStoreName: 'Maintenances',
                baseObject: user.getId()
            }
        }).show();
    },

    onSelectionChange: function (selection, selected) {
        var disabled = selected.length === 0;
        this.lookupReference('userDevicesButton').setDisabled(disabled);
        this.lookupReference('userGroupsButton').setDisabled(disabled);
        this.lookupReference('userGeofencesButton').setDisabled(disabled);
        this.lookupReference('userNotificationsButton').setDisabled(disabled);
        this.lookupReference('userCalendarsButton').setDisabled(disabled);
        this.lookupReference('userAttributesButton').setDisabled(disabled);
        this.lookupReference('userDriversButton').setDisabled(disabled);
        this.lookupReference('userCommandsButton').setDisabled(disabled);
        this.lookupReference('userMaintenancesButton').setDisabled(disabled);
        this.lookupReference('userUsersButton').setDisabled(disabled || selected[0].get('userLimit') === 0);
        this.callParent(arguments);
    }
});
