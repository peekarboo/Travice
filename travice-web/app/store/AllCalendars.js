

Ext.define('Travice.store.AllCalendars', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Calendar',

    proxy: {
        type: 'rest',
        url: 'api/calendars',
        extraParams: {
            all: true
        }
    }
});
