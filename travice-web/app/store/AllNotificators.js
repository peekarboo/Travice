

Ext.define('Travice.store.AllNotificators', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownNotificator',

    proxy: {
        type: 'rest',
        url: 'api/notifications/notificators',
        listeners: {
            exception: function (proxy, response) {
                Travice.app.showError(response);
            }
        }
    }
});
