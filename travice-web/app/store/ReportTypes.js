

Ext.define('Travice.store.ReportTypes', {
    extend: 'Ext.data.Store',
    fields: ['key', 'name'],

    data: [{
        key: 'route',
        name: Strings.reportRoute
    }, {
        key: 'events',
        name: Strings.reportEvents
    }, {
        key: 'trips',
        name: Strings.reportTrips
    }, {
        key: 'stops',
        name: Strings.reportStops
    }, {
        key: 'summary',
        name: Strings.reportSummary
    }, {
        key: 'chart',
        name: Strings.reportChart
    }]
});
