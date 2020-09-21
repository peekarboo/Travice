

Ext.define('Travice.store.Geofences', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Geofence',

    proxy: {
        type: 'rest',
        url: 'api/geofences',
        writer: {
            writeAllFields: true
        }
    }
});
