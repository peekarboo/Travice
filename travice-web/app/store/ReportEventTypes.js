

Ext.define('Travice.store.ReportEventTypes', {
    extend: 'Ext.data.Store',
    fields: ['type', 'name'],

    statics: {
        allEvents: 'allEvents'
    }
});
