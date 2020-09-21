

Ext.define('Travice.store.Statistics', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Statistics',

    proxy: {
        type: 'rest',
        url: 'api/statistics'
    }
});
