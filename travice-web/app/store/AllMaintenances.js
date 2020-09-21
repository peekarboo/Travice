

Ext.define('Travice.store.AllMaintenances', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Maintenance',

    proxy: {
        type: 'rest',
        url: 'api/maintenances',
        extraParams: {
            all: true
        }
    }
});
