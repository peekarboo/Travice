

Ext.define('Travice.store.ReportSummary', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.ReportSummary',

    proxy: {
        type: 'rest',
        url: 'api/reports/summary',
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
