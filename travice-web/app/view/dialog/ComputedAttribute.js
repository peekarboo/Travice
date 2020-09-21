

Ext.define('Travice.view.dialog.ComputedAttribute', {
    extend: 'Travice.view.dialog.BaseEdit',

    requires: [
        'Travice.view.dialog.ComputedAttributeController'
    ],

    controller: 'computedAttribute',
    title: Strings.sharedComputedAttribute,

    items: {
        xtype: 'form',
        items: [{
            xtype: 'textfield',
            name: 'description',
            fieldLabel: Strings.sharedDescription
        }, {
            xtype: 'combobox',
            name: 'attribute',
            fieldLabel: Strings.sharedAttribute,
            store: 'PositionAttributes',
            displayField: 'name',
            valueField: 'key',
            listeners: {
                change: 'onAttributeChange'
            }
        }, {
            xtype: 'textareafield',
            name: 'expression',
            fieldLabel: Strings.sharedExpression,
            allowBlank: false
        }, {
            xtype: 'combobox',
            name: 'type',
            reference: 'typeComboField',
            store: 'AttributeValueTypes',
            fieldLabel: Strings.sharedType,
            displayField: 'name',
            valueField: 'id',
            editable: false
        }]
    },

    buttons: [{
        glyph: 'xf128@FontAwesome',
        tooltip: Strings.sharedCheckComputedAttribute,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'onCheckClick'
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
        handler: 'closeView'
    }]
});
