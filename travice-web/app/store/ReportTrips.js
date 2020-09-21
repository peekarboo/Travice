

Ext.define('Travice.store.ReportTrips', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.ReportTrip',

    proxy: {
        type: 'rest',
        url: 'api/reports/trips',
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
