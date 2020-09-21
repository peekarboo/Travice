

Ext.define('Travice.store.Users', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.User',

    proxy: {
        type: 'rest',
        url: 'api/users',
        writer: {
            writeAllFields: true
        }
    }
});
