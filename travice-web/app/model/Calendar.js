

Ext.define('Travice.model.Calendar', {
    extend: 'Ext.data.Model',
    identifier: 'negative',

    fields: [{
        name: 'id',
        type: 'int'
    }, {
        name: 'name',
        type: 'string'
    }, {
        name: 'data'
    }, {
        name: 'attributes'
    }]
});
