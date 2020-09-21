
Ext.define('Travice.store.GeofenceAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownAttribute',
    proxy: 'memory',

    data: [{
        key: 'color',
        name: Strings.attributeColor,
        valueType: 'color'
    }, {
        key: 'speedLimit',
        name: Strings.attributeSpeedLimit,
        valueType: 'number',
        dataType: 'speed'
    }, {
        key: 'polylineDistance',
        name: Strings.attributePolylineDistance,
        valueType: 'number',
        dataType: 'distance'
    }]
});
