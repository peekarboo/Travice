

Ext.define('Travice.store.ComputedAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.ComputedAttribute',

    proxy: {
        type: 'rest',
        url: 'api/attributes/computed',
        writer: {
            writeAllFields: true
        }
    }
});
