

Ext.define('Travice.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Device',

    proxy: {
        type: 'rest',
        url: 'api/devices',
        writer: {
            writeAllFields: true
        }
    }
});
