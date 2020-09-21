

Ext.define('Travice.store.ReportStops', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.ReportStop',

    proxy: {
        type: 'rest',
        url: 'api/reports/stops',
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
