

Ext.define('Travice.store.AllDrivers', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Driver',

    proxy: {
        type: 'rest',
        url: 'api/drivers',
        extraParams: {
            all: true
        }
    }
});
