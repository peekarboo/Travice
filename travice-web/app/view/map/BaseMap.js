

Ext.define('Travice.view.map.BaseMap', {
    extend: 'Ext.panel.Panel',
    xtype: 'baseMapView',

    layout: 'fit',

    getMap: function () {
        return this.map;
    },

    getMapView: function () {
        return this.mapView;
    },

    initMap: function () {
        var server, layer, type, bingKey, lat, lon, zoom, maxZoom, target, poiLayer;

        server = Travice.app.getServer();

        type = Travice.app.getPreference('map', null);
        bingKey = server.get('bingKey');

        switch (type) {
            case 'custom':
                layer = new ol.layer.Tile({
                    source: new ol.source.XYZ({
                        url: server.get('mapUrl'),
                        attributions: ''
                    })
                });
                break;
            case 'bingRoad':
                layer = new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        key: bingKey,
                        imagerySet: 'Road'
                    })
                });
                break;
            case 'bingAerial':
                layer = new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        key: bingKey,
                        imagerySet: 'Aerial'
                    })
                });
                break;
            case 'bingHybrid':
                layer = new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        key: bingKey,
                        imagerySet: 'AerialWithLabels'
                    })
                });
                break;
            case 'carto':
                layer = new ol.layer.Tile({
                    source: new ol.source.XYZ({
                        url: 'https://cartodb-basemaps-{a-d}.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png',
                        attributions: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> ' +
                            'contributors, &copy; <a href="https://carto.com/attributions">CARTO</a>'
                    })
                });
                break;
            case 'baidu':
                layer = new ol.layer.Tile({
                    source: new ol.source.XYZ({
                        projection: 'BD-MC',
                        tileUrlFunction: function (tileCoord) {
                            var urlsLength = 5, z = tileCoord[0], x = tileCoord[1], y = tileCoord[2], hash, index;

                            hash = (x << z) + y;
                            index = hash % urlsLength;
                            index = index < 0 ? index + urlsLength : index;

                            if (x < 0) {
                                x = 'M' + -x;
                            }
                            if (y < 0) {
                                y = 'M' + -y;
                            }
                            return 'http://online{}.map.bdimg.com/onlinelabel/?qt=tile&x={x}&y={y}&z={z}&styles=pl'
                                .replace('{}', index).replace('{x}', x).replace('{y}', y).replace('{z}', z);
                        },
                        tileGrid: new ol.tilegrid.TileGrid({
                            extent: ol.proj.transformExtent([-180, -74, 180, 74], 'EPSG:4326', 'BD-MC'),
                            origin: [0, 0],
                            minZoom: 3,
                            resolutions: [
                                262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048,
                                1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5
                            ]
                        }),
                        attributions: '&copy; <a href="http://map.baidu.com/">Baidu</a>'
                    })
                });
                break;
            case 'yandexMap':
                layer = new ol.layer.Tile({
                    source: new ol.source.XYZ({
                        url: 'https://vec0{1-4}.maps.yandex.net/tiles?l=map&x={x}&y={y}&z={z}',
                        projection: 'EPSG:3395',
                        attributions: '&copy; <a href="https://yandex.com/maps/">Yandex</a>'
                    })
                });
                break;
            case 'yandexSat':
                layer = new ol.layer.Tile({
                    source: new ol.source.XYZ({
                        url: 'https://sat0{1-4}.maps.yandex.net/tiles?l=sat&x={x}&y={y}&z={z}',
                        projection: 'EPSG:3395',
                        attributions: '&copy; <a href="https://yandex.com/maps/">Yandex</a>'
                    })
                });
                break;
            case 'osm':
                layer = new ol.layer.Tile({
                    source: new ol.source.OSM({})
                });
                break;
            default:
                layer = new ol.layer.Tile({
                    source: new ol.source.OSM({
                        url: 'https://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png'
                    })
                });
                break;
        }

        lat = Travice.app.getPreference('latitude', Travice.Style.mapDefaultLat);
        lon = Travice.app.getPreference('longitude', Travice.Style.mapDefaultLon);
        zoom = Travice.app.getPreference('zoom', Travice.Style.mapDefaultZoom);
        maxZoom = Travice.app.getAttributePreference('web.maxZoom', Travice.Style.mapMaxZoom);

        this.mapView = new ol.View({
            center: ol.proj.fromLonLat([lon, lat]),
            zoom: zoom,
            maxZoom: maxZoom
        });

        this.map = new ol.Map({
            target: this.body.dom.id,
            layers: [layer],
            view: this.mapView
        });

        poiLayer = Travice.app.getPreference('poiLayer', null);

        if (poiLayer) {
            this.map.addLayer(new ol.layer.Vector({
                source: new ol.source.Vector({
                    url: poiLayer,
                    format: new ol.format.KML()
                })
            }));
        }

        this.body.dom.tabIndex = 0;

        switch (Travice.app.getAttributePreference('distanceUnit', 'km')) {
            case 'mi':
                this.map.addControl(new ol.control.ScaleLine({
                    units: 'us'
                }));
                break;
            case 'nmi':
                this.map.addControl(new ol.control.ScaleLine({
                    units: 'nautical'
                }));
                break;
            default:
                this.map.addControl(new ol.control.ScaleLine());
                break;
        }

        target = this.map.getTarget();
        if (typeof target === 'string') {
            target = Ext.get(target).dom;
        }

        this.map.on('pointermove', function (e) {
            var hit = this.forEachFeatureAtPixel(e.pixel, function () {
                return true;
            });
            if (hit) {
                target.style.cursor = 'pointer';
            } else {
                target.style.cursor = '';
            }
        });

        this.map.on('click', function (e) {
            var i, features = this.map.getFeaturesAtPixel(e.pixel, {
                layerFilter: function (layer) {
                    return !layer.get('name');
                }
            });
            if (features) {
                for (i = 0; i < features.length; i++) {
                    this.fireEvent('selectfeature', features[i]);
                }
            } else {
                this.fireEvent('deselectfeature');
            }
        }, this);
    },

    listeners: {
        afterrender: function () {
            this.initMap();
        },

        resize: function () {
            this.map.updateSize();
        }
    }
}, function () {
    proj4.defs('BD-MC', '+proj=merc +lon_0=0 +units=m +ellps=clrk66 +no_defs');
    proj4.defs('EPSG:3395', '+proj=merc +lon_0=0 +k=1 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs');
    ol.proj.get('EPSG:3395').setExtent([-20037508.342789244, -20037508.342789244, 20037508.342789244, 20037508.342789244]);
});
