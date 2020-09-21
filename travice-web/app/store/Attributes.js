

Ext.define('Travice.store.Attributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Attribute',

    sorters: [{
        property: 'priority'
    }]
});
