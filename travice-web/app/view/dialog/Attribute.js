

Ext.define('Travice.view.dialog.Attribute', {
    extend: 'Travice.view.dialog.Base',

    requires: [
        'Travice.view.dialog.AttributeController',
        'Travice.view.ColorPicker',
        'Travice.view.CustomNumberField'
    ],

    controller: 'attribute',
    title: Strings.sharedAttribute,

    items: {
        xtype: 'form',
        listeners: {
            validitychange: 'onValidityChange'
        },
        items: [{
            xtype: 'textfield',
            reference: 'nameTextField',
            name: 'name',
            allowBlank: false,
            fieldLabel: Strings.sharedName
        }, {
            xtype: 'textfield',
            name: 'value',
            reference: 'valueField',
            allowBlank: false,
            fieldLabel: Strings.stateValue
        }]
    },

    buttons: [{
        glyph: 'xf00c@FontAwesome',
        reference: 'saveButton',
        tooltip: Strings.sharedSave,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'onSaveClick'
    }, {
        glyph: 'xf00d@FontAwesome',
        tooltip: Strings.sharedCancel,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'closeView'
    }]
});
