

Ext.define('Travice.store.ReportEvents', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Event',

    proxy: {
        type: 'rest',
        url: 'api/reports/events',
        timeout: Travice.Style.reportTimeout,
        headers: {
            'Accept': 'application/json'
        },
        listeners: {
            exception: function (proxy, exception) {
                Travice.app.showError(exception);
            }
        }
    }
});
