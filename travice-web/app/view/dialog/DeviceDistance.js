

Ext.define('Travice.view.dialog.DeviceDistance', {
    extend: 'Travice.view.dialog.Base',

    requires: [
        'Travice.view.dialog.DeviceDistanceController'
    ],

    controller: 'deviceDistance',
    title: Strings.sharedDeviceDistance,

    items: [{
        xtype: 'customNumberField',
        dataType: 'distance',
        reference: 'totalDistance',
        fieldLabel: Strings.deviceTotalDistance
    }],

    buttons: [{
        reference: 'setButton',
        glyph: 'xf00c@FontAwesome',
        tooltip: Strings.sharedSet,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'onSetClick'
    }, {
        glyph: 'xf00d@FontAwesome',
        tooltip: Strings.sharedCancel,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'closeView'
    }]
});
