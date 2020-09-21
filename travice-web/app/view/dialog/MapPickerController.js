

Ext.define('Travice.view.dialog.MapPickerController', {
    extend: 'Travice.view.dialog.BaseEditController',
    alias: 'controller.mapPicker',

    config: {
        listen: {
            controller: {
                '*': {
                    mapstate: 'setMapState'
                }
            }
        }
    },

    getMapState: function () {
        this.fireEvent('mapstaterequest');
    },

    setMapState: function (lat, lon, zoom) {
        this.lookupReference('latitude').setValue(lat);
        this.lookupReference('longitude').setValue(lon);
        this.lookupReference('zoom').setValue(zoom);
    }
});
