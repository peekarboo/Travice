

Ext.define('Travice.view.map.MapMarkerController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.mapMarker',

    requires: [
        'Travice.model.Position',
        'Travice.model.Device',
        'Travice.DeviceImages'
    ],

    config: {
        listen: {
            controller: {
                '*': {
                    selectdevice: 'selectDevice',
                    selectreport: 'selectReport',
                    selectevent: 'selectEvent'
                },
                'devices': {
                    deselectfeature: 'deselectDevice'
                }
            },
            store: {
                '#Devices': {
                    add: 'updateDevice',
                    update: 'updateDevice',
                    remove: 'removeDevice'
                },
                '#VisibleDevices': {
                    add: 'updateVisibleDevices',
                    update: 'updateVisibleDevices',
                    remove: 'updateVisibleDevices',
                    refresh: 'filterDevices'
                },
                '#LatestPositions': {
                    add: 'updateLatest',
                    update: 'updateLatest'
                },
                '#ReportRoute': {
                    add: 'addReportMarkers',
                    load: 'loadReport',
                    clear: 'clearReport'
                },
                '#Events': {
                    remove: 'clearEvent',
                    clear: 'clearEvent'
                }
            },
            component: {
                '#': {
                    selectfeature: 'selectFeature',
                    deselectfeature: 'deselectFeature'
                }
            }
        }
    },

    init: function () {
        this.latestMarkers = {};
        this.reportMarkers = {};
        this.accuracyCircles = {};
        this.liveRoutes = {};
        this.liveRouteLength = Travice.app.getAttributePreference('web.liveRouteLength', 10);
        this.selectZoom = Travice.app.getAttributePreference('web.selectZoom', 0);
    },

    getAreaStyle: function (label, color) {
        var fillColor, strokeColor, styleConfig;
        if (color) {
            fillColor = ol.color.asArray(color);
            strokeColor = color;
        } else {
            fillColor = ol.color.asArray(Travice.Style.mapGeofenceColor);
            strokeColor = Travice.Style.mapGeofenceColor;
        }
        fillColor[3] = Travice.Style.mapGeofenceOverlayOpacity;
        styleConfig = {
            fill: new ol.style.Fill({
                color: fillColor
            }),
            stroke: new ol.style.Stroke({
                color: strokeColor,
                width: Travice.Style.mapGeofenceWidth
            })
        };
        if (label) {
            styleConfig.text = new ol.style.Text({
                text: label,
                overflow: true,
                fill: new ol.style.Fill({
                    color: Travice.Style.mapGeofenceTextColor
                }),
                stroke: new ol.style.Stroke({
                    color: Travice.Style.mapTextStrokeColor,
                    width: Travice.Style.mapTextStrokeWidth
                }),
                font: Travice.Style.mapTextFont
            });
        }
        return new ol.style.Style(styleConfig);
    },

    getDeviceColor: function (device) {
        switch (device.get('status')) {
            case 'online':
                return Travice.Style.mapColorOnline;
            case 'offline':
                return Travice.Style.mapColorOffline;
            default:
                return Travice.Style.mapColorUnknown;
        }
    },

    updateDevice: function (store, data) {
        var i, device, deviceId, marker, style;

        if (!Ext.isArray(data)) {
            data = [data];
        }

        for (i = 0; i < data.length; i++) {
            device = data[i];
            deviceId = device.get('id');

            if (deviceId in this.latestMarkers) {
                marker = this.latestMarkers[deviceId];
                style = marker.getStyle();
                if (style.getImage().fill !== this.getDeviceColor(device) ||
                        style.getImage().category !== device.get('category')) {
                    this.updateDeviceMarker(style, this.getDeviceColor(device), device.get('category'));
                    marker.changed();
                }
                if (style.getText().getText() !== device.get('name')) {
                    style.getText().setText(device.get('name'));
                    marker.changed();
                }
            }
        }
    },

    removeDevice: function (store, data) {
        var i, deviceId, markersSource;
        if (!Ext.isArray(data)) {
            data = [data];
        }

        markersSource = this.getView().getMarkersSource();

        for (i = 0; i < data.length; i++) {
            deviceId = data[i].get('id');
            if (this.latestMarkers[deviceId]) {
                if (markersSource.getFeatureById(this.latestMarkers[deviceId].getId())) {
                    markersSource.removeFeature(this.latestMarkers[deviceId]);
                }
                delete this.latestMarkers[deviceId];
            }
            if (this.accuracyCircles[deviceId]) {
                if (markersSource.getFeatureById(this.accuracyCircles[deviceId].getId())) {
                    markersSource.removeFeature(this.accuracyCircles[deviceId]);
                }
                delete this.accuracyCircles[deviceId];
            }
            if (this.liveRoutes[deviceId]) {
                if (markersSource.getFeatureById(this.liveRoutes[deviceId].getId())) {
                    markersSource.removeFeature(this.liveRoutes[deviceId]);
                }
                delete this.liveRoutes[deviceId];
            }
        }
    },

    updateLatest: function (store, data) {
        var i, position, device, deviceStore;

        if (!Ext.isArray(data)) {
            data = [data];
        }

        deviceStore = Ext.getStore('Devices');

        for (i = 0; i < data.length; i++) {
            position = data[i];
            device = deviceStore.getById(position.get('deviceId'));

            if (device) {
                this.updateAccuracy(position, device);
                this.updateLatestMarker(position, device);
                this.updateLiveRoute(position, device);
            }
        }
    },

    updateAccuracy: function (position, device) {
        var center, radius, feature, mapView, projection, pointResolution;
        mapView = this.getView().getMapView();
        feature = this.accuracyCircles[position.get('deviceId')];

        if (position.get('accuracy')) {
            projection = mapView.getProjection();
            center = ol.proj.fromLonLat([position.get('longitude'), position.get('latitude')]);
            pointResolution = ol.proj.getPointResolution(projection, mapView.getResolution(), center);
            radius = position.get('accuracy') / ol.proj.METERS_PER_UNIT.m * mapView.getResolution() / pointResolution;

            if (feature) {
                feature.getGeometry().setCenter(center);
                feature.getGeometry().setRadius(radius);
            } else {
                feature = new ol.Feature(new ol.geom.Circle(center, radius));
                feature.setStyle(this.getAreaStyle(null, Travice.Style.mapAccuracyColor));
                feature.setId(position.get('deviceId'));
                this.accuracyCircles[position.get('deviceId')] = feature;
                if (this.isDeviceVisible(device)) {
                    this.getView().getAccuracySource().addFeature(feature);
                }
            }
        } else {
            if (feature && this.getView().getAccuracySource().getFeatureById(feature.getId())) {
                this.getView().getAccuracySource().removeFeature(feature);
            }
            delete this.accuracyCircles[position.get('deviceId')];
        }
    },

    updateLatestMarker: function (position, device) {
        var geometry, deviceId, marker, style;
        geometry = new ol.geom.Point(ol.proj.fromLonLat([
            position.get('longitude'),
            position.get('latitude')
        ]));
        deviceId = position.get('deviceId');
        if (deviceId in this.latestMarkers) {
            marker = this.latestMarkers[deviceId];
            style = marker.getStyle();
            if (style.getImage().angle !== position.get('course')) {
                this.rotateMarker(style, position.get('course'));
            }
            marker.setGeometry(geometry);
        } else {
            marker = new ol.Feature(geometry);
            marker.set('record', device);

            style = this.getLatestMarker(this.getDeviceColor(device),
                position.get('course'),
                device.get('category'));
            style.getText().setText(device.get('name'));
            marker.setStyle(style);
            marker.setId(device.get('id'));
            this.latestMarkers[deviceId] = marker;
            if (this.isDeviceVisible(device)) {
                this.getView().getMarkersSource().addFeature(marker);
            }
        }

        if (marker === this.selectedMarker && this.lookupReference('deviceFollowButton').pressed) {
            this.getView().getMapView().setCenter(marker.getGeometry().getCoordinates());
        }
    },

    updateLiveRoute: function (position, device) {
        var deviceId, liveLine, liveCoordinates, lastLiveCoordinates, newCoordinates;
        deviceId = position.get('deviceId');
        if (deviceId in this.liveRoutes) {
            liveCoordinates = this.liveRoutes[deviceId].getGeometry().getCoordinates();
            lastLiveCoordinates = liveCoordinates[liveCoordinates.length - 1];
            newCoordinates = ol.proj.fromLonLat([position.get('longitude'), position.get('latitude')]);
            if (lastLiveCoordinates[0] === newCoordinates[0] &&
                    lastLiveCoordinates[1] === newCoordinates[1]) {
                return;
            }
            if (liveCoordinates.length >= this.liveRouteLength) {
                liveCoordinates.shift();
            }
            liveCoordinates.push(newCoordinates);
            this.liveRoutes[deviceId].getGeometry().setCoordinates(liveCoordinates);
        } else {
            liveLine = new ol.Feature({
                geometry: new ol.geom.LineString([
                    ol.proj.fromLonLat([
                        position.get('longitude'),
                        position.get('latitude')
                    ])
                ])
            });
            liveLine.setStyle(this.getRouteStyle(deviceId));
            liveLine.setId(deviceId);
            this.liveRoutes[deviceId] = liveLine;
            if (this.isDeviceVisible(device)) {
                this.getView().getMarkersSource().addFeature(liveLine);
            }
        }
    },

    loadReport: function (store, data) {
        var i, position, point, routeSource;
        if (data) {
            this.addReportMarkers(store, data);
            routeSource = this.getView().getRouteSource();

            this.reportRoute = [];
            for (i = 0; i < data.length; i++) {
                position = data[i];
                point = ol.proj.fromLonLat([
                    position.get('longitude'),
                    position.get('latitude')
                ]);
                if (i === 0 || data[i].get('deviceId') !== data[i - 1].get('deviceId')) {
                    this.reportRoute.push(new ol.Feature({
                        geometry: new ol.geom.LineString([])
                    }));
                    this.reportRoute[this.reportRoute.length - 1].setStyle(this.getRouteStyle(data[i].get('deviceId')));
                    routeSource.addFeature(this.reportRoute[this.reportRoute.length - 1]);
                }
                this.reportRoute[this.reportRoute.length - 1].getGeometry().appendCoordinate(point);
            }
        }
    },

    addReportMarker: function (position) {
        var geometry, marker, style, point = ol.proj.fromLonLat([
            position.get('longitude'),
            position.get('latitude')
        ]);
        geometry = new ol.geom.Point(point);
        marker = new ol.Feature(geometry);
        marker.set('record', position);
        style = this.getReportMarker(position.get('deviceId'), position.get('course'));
        marker.setStyle(style);
        this.getView().getMarkersSource().addFeature(marker);
        return marker;
    },

    addReportMarkers: function (store, data) {
        var i;
        this.clearReport();
        for (i = 0; i < data.length; i++) {
            if (store.showMarkers) {
                this.reportMarkers[data[i].get('id')] = this.addReportMarker(data[i]);
            }
        }
        this.zoomToAllPositions(data);
    },

    clearReport: function () {
        var key, i, reportSource, markersSource;

        reportSource = this.getView().getRouteSource();

        if (this.reportRoute) {
            for (i = 0; i < this.reportRoute.length; i++) {
                reportSource.removeFeature(this.reportRoute[i]);
            }
            this.reportRoute = null;
        }

        if (this.reportMarkers) {
            markersSource = this.getView().getMarkersSource();
            for (key in this.reportMarkers) {
                if (this.reportMarkers.hasOwnProperty(key)) {
                    markersSource.removeFeature(this.reportMarkers[key]);
                }
            }
            this.reportMarkers = {};
        }

        if (this.selectedMarker && !this.selectedMarker.get('event') &&
                this.selectedMarker.get('record') instanceof Travice.model.Position) {
            this.selectedMarker = null;
        }
    },

    clearEvent: function () {
        if (this.selectedMarker && this.selectedMarker.get('event')) {
            this.selectMarker(null, false);
        }
    },

    getRouteStyle: function (deviceId) {
        return new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: Travice.app.getReportColor(deviceId),
                width: Travice.Style.mapRouteWidth
            })
        });
    },

    getMarkerStyle: function (zoom, color, angle, category) {
        var image = Travice.DeviceImages.getImageIcon(color, zoom, angle, category);
        return new ol.style.Style({
            image: image,
            text: new ol.style.Text({
                textBaseline: 'bottom',
                fill: new ol.style.Fill({
                    color: Travice.Style.mapTextColor
                }),
                stroke: new ol.style.Stroke({
                    color: Travice.Style.mapTextStrokeColor,
                    width: Travice.Style.mapTextStrokeWidth
                }),
                offsetY: -image.getSize()[1] / 2 - Travice.Style.mapTextOffset,
                font: Travice.Style.mapTextFont
            })
        });
    },

    getLatestMarker: function (color, angle, category) {
        return this.getMarkerStyle(false, color, angle, category);
    },

    getReportMarker: function (deviceId, angle) {
        return this.getMarkerStyle(false, Travice.app.getReportColor(deviceId), angle, 'arrow');
    },

    resizeMarker: function (style, zoom) {
        var image, text;
        image = Travice.DeviceImages.getImageIcon(
            style.getImage().fill, zoom, style.getImage().angle, style.getImage().category);
        text = style.getText();
        text.setOffsetY(-image.getSize()[1] / 2 - Travice.Style.mapTextOffset);
        style.setText(text);
        style.setImage(image);
    },

    rotateMarker: function (style, angle) {
        style.setImage(Travice.DeviceImages.getImageIcon(
            style.getImage().fill, style.getImage().zoom, angle, style.getImage().category));
    },

    updateDeviceMarker: function (style, color, category) {
        var image, text;
        image = Travice.DeviceImages.getImageIcon(
            color, style.getImage().zoom, style.getImage().angle, category);
        text = style.getText();
        text.setOffsetY(-image.getSize()[1] / 2 - Travice.Style.mapTextOffset);
        style.setText(text);
        style.setImage(image);
    },

    selectMarker: function (marker, center) {
        if (this.selectedMarker) {
            if (this.selectedMarker.get('event')) {
                this.getView().getMarkersSource().removeFeature(this.selectedMarker);
                if (!marker || !marker.get('event')) {
                    this.fireEvent('deselectevent');
                }
            } else if (!Ext.getStore('ReportRoute').showMarkers &&
                    this.selectedMarker.get('record') instanceof Travice.model.Position) {
                this.getView().getMarkersSource().removeFeature(this.selectedMarker);
                delete this.reportMarkers[this.selectedMarker.get('record').get('id')];
            } else {
                this.resizeMarker(this.selectedMarker.getStyle(), false);
                this.selectedMarker.getStyle().setZIndex(0);
                this.selectedMarker.changed();
            }
        }

        if (marker) {
            this.resizeMarker(marker.getStyle(), true);
            marker.getStyle().setZIndex(1);
            marker.changed();
            if (center) {
                this.getView().getMapView().setCenter(marker.getGeometry().getCoordinates());
                if (this.selectZoom !== 0 && this.selectZoom > this.getView().getMapView().getZoom()) {
                    this.getView().getMapView().setZoom(this.selectZoom);
                }
            }
        }

        this.selectedMarker = marker;
    },

    selectDevice: function (device, center) {
        this.selectMarker(this.latestMarkers[device.get('id')], center);
    },

    selectReport: function (position, center) {
        if (position instanceof Travice.model.Position) {
            if (!Ext.getStore('ReportRoute').showMarkers) {
                this.reportMarkers[position.get('id')] = this.addReportMarker(position);
            }
            this.selectMarker(this.reportMarkers[position.get('id')], center);
        }
    },

    selectEvent: function (position) {
        var marker;
        this.fireEvent('deselectfeature');
        if (position) {
            marker = this.addReportMarker(position);
            marker.set('event', true);
            this.selectMarker(marker, true);
        } else if (this.selectedMarker && this.selectedMarker.get('event')) {
            this.selectMarker(null, false);
        }
    },

    selectFeature: function (feature) {
        var record = feature.get('record');
        if (record) {
            if (record instanceof Travice.model.Device) {
                this.fireEvent('selectdevice', record, false);
            } else {
                this.fireEvent('selectreport', record, false);
            }
        }
    },

    deselectFeature: function () {
        this.deselectDevice();
        this.fireEvent('deselectfeature');
    },

    deselectDevice: function () {
        this.selectMarker(null, false);
    },

    zoomToAllPositions: function (data) {
        var i, point, minx, miny, maxx, maxy;
        for (i = 0; i < data.length; i++) {
            point = ol.proj.fromLonLat([
                data[i].get('longitude'),
                data[i].get('latitude')
            ]);
            if (i === 0) {
                minx = maxx = point[0];
                miny = maxy = point[1];
            } else {
                minx = Math.min(point[0], minx);
                miny = Math.min(point[1], miny);
                maxx = Math.max(point[0], maxx);
                maxy = Math.max(point[1], maxy);
            }
        }
        if (minx !== maxx || miny !== maxy) {
            this.getView().getMapView().fit([minx, miny, maxx, maxy]);
        } else if (point) {
            this.getView().getMapView().fit(new ol.geom.Point(point));
        }
    },

    updateVisibleDevices: function (store, data) {
        var i, device;

        if (!Ext.isArray(data)) {
            data = [data];
        }

        for (i = 0; i < data.length; i++) {
            device = data[i];
            if (device.get('id') in this.latestMarkers) {
                this.updateDeviceVisibility(device);
            }
        }
    },

    isDeviceVisible: function (device) {
        return Ext.getStore('VisibleDevices').contains(device);
    },

    updateDeviceVisibility: function (device) {
        var deviceId, accuracy, liveLine, marker;
        deviceId = device.get('id');
        marker = this.latestMarkers[deviceId];
        accuracy = this.accuracyCircles[deviceId];
        liveLine = this.liveRoutes[deviceId];
        if (this.isDeviceVisible(device)) {
            if (marker && !this.getView().getMarkersSource().getFeatureById(marker.getId())) {
                this.getView().getMarkersSource().addFeature(marker);
            }
            if (accuracy && !this.getView().getAccuracySource().getFeatureById(accuracy.getId())) {
                this.getView().getAccuracySource().addFeature(accuracy);
            }
            if (liveLine && !this.getView().getLiveRouteSource().getFeatureById(liveLine.getId())) {
                this.getView().getLiveRouteSource().addFeature(liveLine);
            }
        } else {
            if (marker && this.getView().getMarkersSource().getFeatureById(marker.getId())) {
                this.getView().getMarkersSource().removeFeature(marker);
            }
            if (accuracy && this.getView().getAccuracySource().getFeatureById(accuracy.getId())) {
                this.getView().getAccuracySource().removeFeature(accuracy);
            }
            if (liveLine && this.getView().getLiveRouteSource().getFeatureById(liveLine.getId())) {
                this.getView().getLiveRouteSource().removeFeature(liveLine);
            }
        }
    },

    filterDevices: function () {
        Ext.getStore('Devices').each(this.updateDeviceVisibility, this, false);
    }
});
