

Ext.define('Travice.view.StateController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.state',

    requires: [
        'Travice.AttributeFormatter',
        'Travice.model.Attribute',
        'Travice.model.Position',
        'Travice.view.BaseWindow',
        'Travice.view.edit.ComputedAttributes'
    ],

    config: {
        listen: {
            controller: {
                '*': {
                    selectdevice: 'selectDevice',
                    selectreport: 'selectReport',
                    deselectfeature: 'deselectFeature'
                }
            },
            global: {
                stategeocode: 'onGeocode'
            },
            store: {
                '#LatestPositions': {
                    add: 'updateLatest',
                    update: 'updateLatest'
                },
                '#ReportRoute': {
                    clear: 'clearReport'
                }
            }
        }
    },

    init: function () {
        var i, hideAttributesPreference, attributesList;
        if (Travice.app.getUser().get('administrator') ||
                !Travice.app.getUser().get('deviceReadonly') && !Travice.app.getPreference('readonly', false)) {
            this.lookupReference('computedAttributesButton').setDisabled(
                Travice.app.getBooleanAttributePreference('ui.disableComputedAttributes'));
        }
        hideAttributesPreference = Travice.app.getAttributePreference('ui.hidePositionAttributes');
        this.hideAttributes = {};
        if (hideAttributesPreference) {
            attributesList = hideAttributesPreference.split(/[ ,]+/).filter(Boolean);
            for (i = 0; i < attributesList.length; i++) {
                this.hideAttributes[attributesList[i]] = true;
            }
        }
    },

    onComputedAttributesClick: function () {
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedComputedAttributes,
            items: {
                xtype: 'computedAttributesView'
            }
        }).show();
    },

    keys: (function () {
        var i, list, result;
        result = {};
        list = ['fixTime', 'latitude', 'longitude', 'valid', 'accuracy', 'altitude', 'speed', 'course', 'address', 'protocol'];
        for (i = 0; i < list.length; i++) {
            result[list[i]] = {
                priority: i,
                name: Strings['position' + list[i].replace(/^\w/g, function (s) {
                    return s.toUpperCase();
                })]
            };
        }
        return result;
    })(),

    updateLatest: function (store, data) {
        var i;
        if (!Ext.isArray(data)) {
            data = [data];
        }
        for (i = 0; i < data.length; i++) {
            if (this.deviceId === data[i].get('deviceId')) {
                this.position = data[i];
                this.updatePosition();
            }
        }
    },

    formatValue: function (value) {
        if (typeof id === 'number') {
            return Number(value.toFixed(2));
        } else {
            return value;
        }
    },

    findAttribute: function (record) {
        return record.get('deviceId') === this.position.get('deviceId') && record.get('attribute') === this.lookupAttribute;
    },

    updatePosition: function () {
        var attributes, store, key, name, value;
        store = Ext.getStore('Attributes');
        store.removeAll();

        for (key in this.position.data) {
            if (this.position.data.hasOwnProperty(key) && this.keys[key] !== undefined) {
                store.add(Ext.create('Travice.model.Attribute', {
                    priority: this.keys[key].priority,
                    name: this.keys[key].name,
                    value: Travice.AttributeFormatter.getFormatter(key)(this.position.get(key))
                }));
            }
        }

        attributes = this.position.get('attributes');
        if (attributes instanceof Object) {
            for (key in attributes) {
                if (attributes.hasOwnProperty(key) && !this.hideAttributes[key]) {
                    this.lookupAttribute = key;
                    name = Ext.getStore('PositionAttributes').getAttributeName(key, true);
                    if (this.position.get('attribute.' + key) !== undefined) {
                        value = Travice.AttributeFormatter.getAttributeFormatter(key)(this.position.get('attribute.' + key));
                    } else {
                        value = Travice.AttributeFormatter.defaultFormatter(attributes[key]);
                    }
                    store.add(Ext.create('Travice.model.Attribute', {
                        priority: 1024,
                        name: name,
                        attribute: key,
                        value: value
                    }));
                }
            }
        }
    },

    selectDevice: function (device) {
        var position;
        this.deviceId = device.get('id');
        position = Ext.getStore('LatestPositions').findRecord('deviceId', this.deviceId, 0, false, false, true);
        if (position) {
            this.position = position;
            this.updatePosition();
        } else {
            this.position = null;
            Ext.getStore('Attributes').removeAll();
        }
    },

    selectReport: function (position) {
        if (position instanceof Travice.model.Position) {
            this.deviceId = null;
            this.position = position;
            this.updatePosition();
        }
    },

    deselectFeature: function () {
        this.deviceId = null;
        this.position = null;
        Ext.getStore('Attributes').removeAll();
    },

    clearReport: function () {
        if (!this.deviceId) {
            this.position = null;
            Ext.getStore('Attributes').removeAll();
        }
    },

    onGeocode: function () {
        var positionId = this.position.getId();
        if (!this.position.get('address')) {
            Ext.Ajax.request({
                scope: this,
                method: 'GET',
                url: 'api/server/geocode',
                params: {
                    latitude: this.position.get('latitude'),
                    longitude: this.position.get('longitude')
                },
                success: function (response) {
                    if (this.position && this.position.getId() === positionId) {
                        this.position.set('address', response.responseText);
                        this.updatePosition();
                    }
                },
                failure: function (response) {
                    Travice.app.showError(response);
                }
            });
        }
    }
});
