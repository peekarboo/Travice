

Ext.define('Travice.store.AllCommandTypes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownCommand',

    proxy: {
        type: 'rest',
        url: 'api/commands/types',
        listeners: {
            exception: function (proxy, response) {
                Travice.app.showError(response);
            }
        }
    }
});
