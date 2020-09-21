

Ext.define('Travice.store.ReportRoute', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Position',

    proxy: {
        type: 'rest',
        url: 'api/reports/route',
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
