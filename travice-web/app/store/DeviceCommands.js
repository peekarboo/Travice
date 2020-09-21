

Ext.define('Travice.store.DeviceCommands', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Command',

    proxy: {
        type: 'rest',
        url: 'api/commands/send',
        listeners: {
            'exception': function (proxy, response) {
                Travice.app.showError(response);
            }
        }
    }
});
