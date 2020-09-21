
Ext.define('Travice.model.KnownNotification', {
    extend: 'Ext.data.Model',
    idProperty: 'type',

    fields: [{
        name: 'type',
        type: 'string'
    }, {
        name: 'name',
        convert: function (v, rec) {
            return Travice.app.getEventString(rec.get('type'));
        },
        depends: ['type']
    }]
});
