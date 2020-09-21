

Ext.define('Travice.view.edit.GeofencesController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.geofences',

    requires: [
        'Travice.view.dialog.Geofence',
        'Travice.model.Geofence'
    ],

    objectModel: 'Travice.model.Geofence',
    objectDialog: 'Travice.view.dialog.Geofence',
    removeTitle: Strings.sharedGeofence
});
