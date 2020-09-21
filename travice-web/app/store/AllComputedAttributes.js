

Ext.define('Travice.store.AllComputedAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.ComputedAttribute',

    proxy: {
        type: 'rest',
        url: 'api/attributes/computed',
        extraParams: {
            all: true
        }
    }
});
