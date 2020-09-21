

Ext.define('Travice.store.Commands', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Command',

    proxy: {
        type: 'rest',
        url: 'api/commands',
        writer: {
            writeAllFields: true
        }
    }
});
