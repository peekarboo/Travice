

Ext.define('Travice.store.AllGeofences', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Geofence',

    proxy: {
        type: 'rest',
        url: 'api/geofences',
        extraParams: {
            all: true
        }
    }
});
