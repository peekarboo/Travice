

Ext.define('Travice.store.AllCommands', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Command',

    proxy: {
        type: 'rest',
        url: 'api/commands',
        extraParams: {
            all: true
        }
    }
});
