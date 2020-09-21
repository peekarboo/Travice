

Ext.define('Travice.view.dialog.Device', {
    extend: 'Travice.view.dialog.BaseEdit',

    requires: [
        'Travice.view.ClearableComboBox',
        'Travice.view.dialog.DeviceController'
    ],

    controller: 'device',
    title: Strings.sharedDevice,

    items: {
        xtype: 'form',
        items: [{
            xtype: 'fieldset',
            title: Strings.sharedRequired,
            items: [{
                xtype: 'textfield',
                name: 'name',
                fieldLabel: Strings.sharedName,
                allowBlank: false
            }, {
                xtype: 'textfield',
                name: 'uniqueId',
                fieldLabel: Strings.deviceIdentifier,
                allowBlank: false
            }]
        }, {
            xtype: 'fieldset',
            title: Strings.sharedExtra,
            collapsible: true,
            collapsed: true,
            items: [{
                xtype: 'clearableComboBox',
                name: 'groupId',
                fieldLabel: Strings.groupParent,
                store: 'Groups',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id'
            }, {
                xtype: 'textfield',
                name: 'phone',
                fieldLabel: Strings.sharedPhone
            }, {
                xtype: 'textfield',
                name: 'model',
                fieldLabel: Strings.deviceModel
            }, {
                xtype: 'textfield',
                name: 'contact',
                fieldLabel: Strings.deviceContact
            }, {
                xtype: 'combobox',
                name: 'category',
                fieldLabel: Strings.deviceCategory,
                store: 'DeviceImages',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'key',
                editable: false,
                listConfig: {
                    getInnerTpl: function () {
                        return '<table><tr valign="middle" ><td><div align="center" style="width:40px;height:40px;" >' +
                        '{[new XMLSerializer().serializeToString(Travice.DeviceImages.getImageSvg(' +
                        'Travice.Style.mapColorOnline, false, 0, values.key))]}</div></td>' +
                        '<td>{name}</td></tr></table>';
                    }
                }
            }, {
                xtype: 'checkboxfield',
                inputValue: true,
                uncheckedValue: false,
                name: 'disabled',
                fieldLabel: Strings.sharedDisabled,
                hidden: true,
                reference: 'disabledField'
            }]
        }]
    }
});
