
Ext.define('Travice.store.GroupAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownAttribute',

    data: [{
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
