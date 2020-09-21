

Ext.define('Travice.view.dialog.Maintenance', {
    extend: 'Travice.view.dialog.BaseEdit',

    requires: [
        'Travice.view.dialog.MaintenanceController',
        'Travice.view.CustomNumberField'
    ],

    controller: 'maintenance',

    title: Strings.sharedMaintenance,

    items: {
        xtype: 'form',
        listeners: {
            validitychange: 'onValidityChange'
        },
        items: [{
            xtype: 'fieldset',
            title: Strings.sharedRequired,
            items: [{
                xtype: 'textfield',
                name: 'name',
                fieldLabel: Strings.sharedName,
                allowBlank: false
            }, {
                xtype: 'combobox',
                name: 'type',
                reference: 'typeComboField',
                fieldLabel: Strings.sharedType,
                displayField: 'name',
                valueField: 'key',
                allowBlank: false,
                queryMode: 'local',
                store: 'MaintenanceTypes',
                listeners: {
                    change: 'onNameChange'
                }
            }, {
                xtype: 'customNumberField',
                name: 'start',
                reference: 'startField',
                fieldLabel: Strings.maintenanceStart
            }, {
                xtype: 'customNumberField',
                name: 'period',
                reference: 'periodField',
                allowBlank: false,
                fieldLabel: Strings.maintenancePeriod,
                validator: function (value) {
                    return this.parseValue(value) !== 0 ? true : Strings.errorZero;
                }
            }]
        }]
    }
});
