

Ext.define('Travice.store.Maintenances', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Maintenance',

    proxy: {
        type: 'rest',
        url: 'api/maintenances',
        writer: {
            writeAllFields: true
        }
    }
});
