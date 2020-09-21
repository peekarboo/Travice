

Ext.define('Travice.view.dialog.GeofenceController', {
    extend: 'Travice.view.dialog.BaseEditController',
    alias: 'controller.geofence',

    requires: [
        'Travice.view.BaseWindow',
        'Travice.view.map.GeofenceMap'
    ],

    config: {
        listen: {
            controller: {
                '*': {
                    savearea: 'saveArea'
                }
            }
        }
    },

    init: function () {
        this.lookupReference('calendarCombo').setHidden(
            Travice.app.getBooleanAttributePreference('ui.disableCalendars'));
    },

    saveArea: function (value) {
        this.lookupReference('areaField').setValue(value);
    },

    onAreaClick: function (button) {
        var dialog, record;
        dialog = button.up('window').down('form');
        record = dialog.getRecord();
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedArea,
            items: {
                xtype: 'geofenceMapView',
                area: record.get('area')
            }
        }).show();
    }
});
