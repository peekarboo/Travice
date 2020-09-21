

Ext.define('Travice.store.AllNotifications', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Notification',

    proxy: {
        type: 'rest',
        url: 'api/notifications',
        extraParams: {
            all: true
        }
    }
});
