

Ext.define('Travice.store.AllNotificationTypes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownNotification',

    proxy: {
        type: 'rest',
        url: 'api/notifications/types',
        listeners: {
            exception: function (proxy, response) {
                Travice.app.showError(response);
            }
        }
    }
});
