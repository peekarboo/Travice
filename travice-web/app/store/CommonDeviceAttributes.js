
Ext.define('Travice.store.CommonDeviceAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownAttribute',

    data: [{
        key: 'speedLimit',
        name: Strings.attributeSpeedLimit,
        valueType: 'number',
        dataType: 'speed'
    }, {
        key: 'report.ignoreOdometer',
        name: Strings.attributeReportIgnoreOdometer,
        valueType: 'boolean'
    }]
});
