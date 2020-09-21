

Ext.define('Travice.view.DeviceMenu', {
    extend: 'Ext.button.Button',
    xtype: 'deviceMenu',

    requires: [
        'Travice.view.DeviceMenuController'
    ],

    glyph: 'xf013@FontAwesome',
    tooltip: Strings.settingsTitle,
    tooltipType: 'title',

    menu: {
        controller: 'deviceMenu',

        items: [{
            text: Strings.sharedDrivers,
            glyph: 'xf2c2@FontAwesome',
            handler: 'onDriversClick',
            reference: 'menuDriversButton'
        }, {
            text: Strings.sharedGeofences,
            glyph: 'xf21d@FontAwesome',
            handler: 'onGeofencesClick',
            reference: 'menuGeofencesButton'
        }, {
            text: Strings.sharedNotifications,
            glyph: 'xf003@FontAwesome',
            handler: 'onNotificationsClick',
            reference: 'menuNotificationsButton'
        }, {
            text: Strings.sharedComputedAttributes,
            glyph: 'xf0ae@FontAwesome',
            handler: 'onComputedAttributesClick',
            reference: 'menuComputedAttributesButton'
        }, {
            text: Strings.sharedSavedCommands,
            glyph: 'xf093@FontAwesome',
            handler: 'onCommandsClick',
            reference: 'menuCommandsButton'
        }, {
            text: Strings.sharedMaintenances,
            glyph: 'xf0ad@FontAwesome',
            handler: 'onMaintenancesClick',
            reference: 'menuMaintenancesButton'
        }, {
            hidden: true,
            text: Strings.sharedDeviceDistance,
            glyph: 'xf0e4@FontAwesome',
            handler: 'onDeviceDistanceClick',
            reference: 'menuDeviceDistanceButton'
        }]
    }
});
