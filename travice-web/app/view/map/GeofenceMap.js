

Ext.define('Travice.view.map.GeofenceMap', {
    extend: 'Travice.view.map.BaseMap',
    xtype: 'geofenceMapView',

    requires: [
        'Travice.view.map.GeofenceMapController',
        'Travice.GeofenceConverter'
    ],

    controller: 'geofenceMap',
    bodyBorder: true,

    tbar: {
        items: [{
            xtype: 'combobox',
            store: 'GeofenceTypes',
            valueField: 'key',
            displayField: 'name',
            editable: false,
            listeners: {
                select: 'onTypeSelect'
            }
        }, {
            xtype: 'tbfill'
        }, {
            glyph: 'xf00c@FontAwesome',
            tooltip: Strings.sharedSave,
            tooltipType: 'title',
            minWidth: 0,
            handler: 'onSaveClick'
        }, {
            glyph: 'xf00d@FontAwesome',
            tooltip: Strings.sharedCancel,
            tooltipType: 'title',
            minWidth: 0,
            handler: 'onCancelClick'
        }]
    },

    getFeatures: function () {
        return this.features;
    },

    initMap: function () {
        var map, featureOverlay, geometry, fillColor;
        this.callParent();

        map = this.map;

        this.features = new ol.Collection();
        if (this.area) {
            geometry = Travice.GeofenceConverter.wktToGeometry(this.mapView, this.area);
            this.features.push(new ol.Feature(geometry));
            this.mapView.fit(geometry);
        } else {
            this.controller.fireEvent('mapstaterequest');
        }
        fillColor = ol.color.asArray(Travice.Style.mapGeofenceColor);
        fillColor[3] = Travice.Style.mapGeofenceOverlayOpacity;
        featureOverlay = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: this.features
            }),
            style: new ol.style.Style({
                fill: new ol.style.Fill({
                    color: fillColor
                }),
                stroke: new ol.style.Stroke({
                    color: Travice.Style.mapGeofenceColor,
                    width: Travice.Style.mapGeofenceWidth
                }),
                image: new ol.style.Circle({
                    radius: Travice.Style.mapGeofenceRadius,
                    fill: new ol.style.Fill({
                        color: Travice.Style.mapGeofenceColor
                    })
                })
            })
        });
        featureOverlay.setMap(map);

        map.addInteraction(new ol.interaction.Modify({
            features: this.features,
            deleteCondition: function (event) {
                return ol.events.condition.shiftKeyOnly(event) && ol.events.condition.singleClick(event);
            }
        }));
    },

    addInteraction: function (type) {
        this.draw = new ol.interaction.Draw({
            features: this.features,
            type: type
        });
        this.draw.on('drawstart', function () {
            this.features.clear();
        }, this);
        this.map.addInteraction(this.draw);
    },

    removeInteraction: function () {
        if (this.draw) {
            this.map.removeInteraction(this.draw);
            this.draw = null;
        }
    }
});
