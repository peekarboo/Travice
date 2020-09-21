

Ext.define('Travice.view.dialog.DeviceDistanceController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.deviceDistance',

    onSetClick: function () {
        var data = {
            deviceId: this.getView().deviceId,
            totalDistance: this.lookupReference('totalDistance').getValue()
        };
        Ext.Ajax.request({
            scope: this,
            method: 'PUT',
            url: 'api/devices/' + data.deviceId + '/distance',
            jsonData: Ext.util.JSON.encode(data),
            callback: function (options, success, response) {
                if (!success) {
                    Travice.app.showError(response);
                }
            }
        });
        this.closeView();
    }
});
