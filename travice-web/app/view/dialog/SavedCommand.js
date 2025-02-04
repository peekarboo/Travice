

Ext.define('Travice.view.dialog.SavedCommand', {
    extend: 'Travice.view.dialog.BaseEdit',

    requires: [
        'Travice.view.dialog.SavedCommandController'
    ],

    controller: 'savedCommand',
    title: Strings.sharedSavedCommand,

    items: [{
        xtype: 'form',
        listeners: {
            validitychange: 'onValidityChange'
        },
        items: [{
            xtype: 'fieldset',
            title: Strings.sharedRequired,
            items: [{
                xtype: 'textfield',
                name: 'description',
                fieldLabel: Strings.sharedDescription
            }, {
                xtype: 'checkboxfield',
                name: 'textChannel',
                inputValue: true,
                uncheckedValue: false,
                fieldLabel: Strings.notificationSms
            }, {
                xtype: 'combobox',
                name: 'type',
                reference: 'commandType',
                fieldLabel: Strings.sharedType,
                store: 'AllCommandTypes',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'type',
                editable: false,
                allowBlank: false,
                listeners: {
                    change: 'onTypeChange'
                }
            }, {
                xtype: 'fieldcontainer',
                reference: 'parameters'
            }]
        }]
    }],

    buttons: [{
        glyph: 'xf00c@FontAwesome',
        reference: 'saveButton',
        tooltip: Strings.sharedSave,
        tooltipType: 'title',
        minWidth: 0,
        disabled: true,
        handler: 'onSaveClick'
    }, {
        glyph: 'xf00d@FontAwesome',
        tooltip: Strings.sharedCancel,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'closeView'
    }]
});
