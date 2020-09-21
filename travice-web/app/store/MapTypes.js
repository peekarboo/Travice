

Ext.define('Travice.store.MapTypes', {
    extend: 'Ext.data.Store',
    fields: ['key', 'name'],

    data: [{
        key: 'carto',
        name: Strings.mapCarto
    }, {
        key: 'osm',
        name: Strings.mapOsm
    }, {
        key: 'bingRoad',
        name: Strings.mapBingRoad
    }, {
        key: 'bingAerial',
        name: Strings.mapBingAerial
    }, {
        key: 'bingHybrid',
        name: Strings.mapBingHybrid
    }, {
        key: 'baidu',
        name: Strings.mapBaidu
    }, {
        key: 'yandexMap',
        name: Strings.mapYandexMap
    }, {
        key: 'yandexSat',
        name: Strings.mapYandexSat
    }, {
        key: 'wikimedia',
        name: Strings.mapWikimedia
    }, {
        key: 'custom',
        name: Strings.mapCustom
    }]
});
