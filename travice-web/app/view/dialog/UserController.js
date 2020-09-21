

Ext.define('Travice.view.dialog.UserController', {
    extend: 'Travice.view.dialog.MapPickerController',
    alias: 'controller.user',

    init: function () {
        if (Travice.app.getUser().get('administrator')) {
            this.lookupReference('adminField').setDisabled(false);
            this.lookupReference('deviceLimitField').setDisabled(false);
            this.lookupReference('userLimitField').setDisabled(false);
        }
        if (Travice.app.getUser().get('administrator') || !this.getView().selfEdit) {
            this.lookupReference('readonlyField').setDisabled(false);
            this.lookupReference('disabledField').setDisabled(false);
            this.lookupReference('expirationTimeField').setDisabled(false);
            this.lookupReference('deviceReadonlyField').setDisabled(false);
            this.lookupReference('limitCommandsField').setDisabled(false);
        }
    },

    symbols: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789',

    generateToken: function () {
        var i, newToken = '';

        for (i = 0; i < 32; i++) {
            newToken += this.symbols.charAt(Math.floor(Math.random() * this.symbols.length));
        }

        this.lookupReference('tokenField').setValue(newToken);
    },

    testNotification: function () {
        Ext.Ajax.request({
            url: 'api/notifications/test',
            method: 'POST',
            failure: function (response) {
                Travice.app.showError(response);
            }
        });
    },

    onSaveClick: function (button) {
        var dialog, record, store;
        dialog = button.up('window').down('form');
        dialog.updateRecord();
        record = dialog.getRecord();
        if (record === Travice.app.getUser()) {
            record.save();
        } else {
            store = Ext.getStore('Users');
            if (record.phantom) {
                store.add(record);
            }
            store.sync({
                failure: function (batch) {
                    store.rejectChanges();
                    Travice.app.showError(batch.exceptions[0].getError().response);
                }
            });
        }
        button.up('window').close();
    }
});
