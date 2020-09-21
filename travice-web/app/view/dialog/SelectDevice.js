
Ext.define('Travice.view.dialog.SelectDevice', {
    extend: 'Travice.view.dialog.Base',

    requires: [
        'Travice.view.dialog.SelectDeviceController'
    ],

    controller: 'selectDevice',
    title: Strings.sharedDevice,

    items: {
        xtype: 'form',
        items: [{
            xtype: 'combobox',
            reference: 'deviceField',
            store: 'Devices',
            queryMode: 'local',
            displayField: 'name',
            valueField: 'id',
            editable: false,
            listeners: {
                change: 'onDeviceChange'
            }
        }]
    },

    buttons: [{
        glyph: 'xf00c@FontAwesome',
        reference: 'saveButton',
        tooltip: Strings.sharedSave,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'onSaveClick',
        disabled: true
    }, {
        glyph: 'xf00d@FontAwesome',
        tooltip: Strings.sharedCancel,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'closeView'
    }]
});
