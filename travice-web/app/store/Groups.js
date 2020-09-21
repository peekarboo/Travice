

Ext.define('Travice.store.Groups', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Group',

    proxy: {
        type: 'rest',
        url: 'api/groups',
        writer: {
            writeAllFields: true
        }
    }
});
