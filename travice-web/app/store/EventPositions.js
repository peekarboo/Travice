

Ext.define('Travice.store.EventPositions', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Position',

    trackRemoved: false,

    proxy: {
        type: 'rest',
        url: 'api/positions',
        headers: {
            'Accept': 'application/json'
        }
    }
});
