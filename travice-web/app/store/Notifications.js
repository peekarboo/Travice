

Ext.define('Travice.store.Notifications', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Notification',

    proxy: {
        type: 'rest',
        url: 'api/notifications',
        writer: {
            writeAllFields: true
        }
    }
});
