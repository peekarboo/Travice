
Ext.define('Travice.store.CommonUserAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownAttribute',

    data: [{
        key: 'web.liveRouteLength',
        name: Strings.attributeWebLiveRouteLength,
        valueType: 'number',
        allowDecimals: false
    }, {
        key: 'web.selectZoom',
        name: Strings.attributeWebSelectZoom,
        valueType: 'number',
        allowDecimals: false,
        minValue: Travice.Style.mapDefaultZoom,
        maxValue: Travice.Style.mapMaxZoom
    }, {
        key: 'web.maxZoom',
        name: Strings.attributeWebMaxZoom,
        valueType: 'number',
        allowDecimals: false,
        minValue: Travice.Style.mapDefaultZoom,
        maxValue: Travice.Style.mapMaxZoom
    }, {
        key: 'ui.disableReport',
        name: Strings.attributeUiDisableReport,
        valueType: 'boolean'
    }, {
        key: 'ui.disableEvents',
        name: Strings.attributeUiDisableEvents,
        valueType: 'boolean'
    }, {
        key: 'ui.disableVehicleFetures',
        name: Strings.attributeUiDisableVehicleFetures,
        valueType: 'boolean'
    }, {
        key: 'ui.disableDrivers',
        name: Strings.attributeUiDisableDrivers,
        valueType: 'boolean'
    }, {
        key: 'ui.disableComputedAttributes',
        name: Strings.attributeUiDisableComputedAttributes,
        valueType: 'boolean'
    }, {
        key: 'ui.disableCalendars',
        name: Strings.attributeUiDisableCalendars,
        valueType: 'boolean'
    }, {
        key: 'ui.disableMaintenances',
        name: Strings.attributeUiDisableMaintenances,
        valueType: 'boolean'
    }, {
        key: 'ui.hidePositionAttributes',
        name: Strings.attributeUiHidePositionAttributes,
        valueType: 'string'
    }, {
        key: 'distanceUnit',
        name: Strings.settingsDistanceUnit,
        valueType: 'string',
        dataType: 'distanceUnit'
    }, {
        key: 'speedUnit',
        name: Strings.settingsSpeedUnit,
        valueType: 'string',
        dataType: 'speedUnit'
    }, {
        key: 'volumeUnit',
        name: Strings.settingsVolumeUnit,
        valueType: 'string',
        dataType: 'volumeUnit'
    }, {
        key: 'timezone',
        name: Strings.sharedTimezone,
        valueType: 'string',
        dataType: 'timezone'
    }]
});
