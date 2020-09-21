

Ext.define('Travice.store.Drivers', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Driver',

    proxy: {
        type: 'rest',
        url: 'api/drivers',
        writer: {
            writeAllFields: true
        }
    }
});
