
Ext.define('Travice.model.KnownNotificator', {
    extend: 'Ext.data.Model',
    idProperty: 'type',

    fields: [{
        name: 'type',
        type: 'string'
    }, {
        name: 'name',
        convert: function (v, rec) {
            return Travice.app.getNotificatorString(rec.get('type'));
        },
        depends: ['type']
    }]
});
