

Ext.define('Travice.store.AllDevices', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Device',

    proxy: {
        type: 'rest',
        url: 'api/devices',
        extraParams: {
            all: true
        }
    }
});
