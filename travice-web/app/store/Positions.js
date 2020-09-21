

Ext.define('Travice.store.Positions', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Position',

    proxy: {
        type: 'rest',
        url: 'api/positions',
        headers: {
            'Accept': 'application/json'
        }
    }
});
