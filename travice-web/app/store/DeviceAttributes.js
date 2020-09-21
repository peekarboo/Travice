
Ext.define('Travice.store.DeviceAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownAttribute',

    data: [{
        key: 'web.reportColor',
        name: Strings.attributeWebReportColor,
        valueType: 'color'
    }, {
        key: 'devicePassword',
        name: Strings.attributeDevicePassword,
        valueType: 'string'
    }, {
        key: 'processing.copyAttributes',
        name: Strings.attributeProcessingCopyAttributes,
        valueType: 'string'
    }, {
        key: 'decoder.timezone',
        name: Strings.sharedTimezone,
        valueType: 'string',
        dataType: 'timezone'
    }]
});
