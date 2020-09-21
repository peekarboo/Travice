

Ext.define('Travice.view.dialog.DeviceController', {
    extend: 'Travice.view.dialog.BaseEditController',
    alias: 'controller.device',

    init: function () {
        if (Travice.app.getUser().get('administrator')) {
            this.lookupReference('disabledField').setHidden(false);
        }
    }

});
