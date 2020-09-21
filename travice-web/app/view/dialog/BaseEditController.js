

Ext.define('Travice.view.dialog.BaseEditController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.baseEdit',

    requires: [
        'Travice.view.BaseWindow',
        'Travice.view.edit.Attributes'
    ],

    onSaveClick: function (button) {
        var dialog, store, record;
        dialog = button.up('window').down('form');
        dialog.updateRecord();
        record = dialog.getRecord();
        store = record.store;
        if (store) {
            if (record.phantom) {
                store.add(record);
            }
            store.sync({
                failure: function (batch) {
                    store.rejectChanges();
                    Travice.app.showError(batch.exceptions[0].getError().response);
                }
            });
        } else {
            record.save();
        }
        this.closeView();
    },

    showAttributesView: function (button) {
        var dialog, record;
        dialog = button.up('window').down('form');
        record = dialog.getRecord();
        Ext.create('Travice.view.BaseWindow', {
            title: Strings.sharedAttributes,
            items: {
                xtype: 'attributesView',
                record: record
            }
        }).show();
    }
});
