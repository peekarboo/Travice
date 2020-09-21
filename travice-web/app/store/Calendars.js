

Ext.define('Travice.store.Calendars', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Calendar',

    proxy: {
        type: 'rest',
        url: 'api/calendars',
        writer: {
            writeAllFields: true
        }
    }
});
