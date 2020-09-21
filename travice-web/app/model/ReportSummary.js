

Ext.define('Travice.model.ReportSummary', {
    extend: 'Ext.data.Model',
    identifier: 'negative',

    fields: [{
        name: 'deviceId',
        type: 'int'
    }, {
        name: 'deviceName',
        type: 'string'
    }, {
        name: 'maxSpeed',
        type: 'float',
        convert: Travice.AttributeFormatter.getConverter('speed')
    }, {
        name: 'averageSpeed',
        type: 'float',
        convert: Travice.AttributeFormatter.getConverter('speed')
    }, {
        name: 'distance',
        type: 'float',
        convert: Travice.AttributeFormatter.getConverter('distance')
    }, {
        name: 'engineHours',
        type: 'int'
    }, {
        name: 'spentFuel',
        type: 'float',
        convert: Travice.AttributeFormatter.getConverter('spentFuel')
    }]
});
