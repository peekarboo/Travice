

Ext.define('Travice.store.Events', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Event',

    trackRemoved: false,

    proxy: {
        type: 'rest',
        url: 'api/events'
    }
});
