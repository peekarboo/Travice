

Ext.define('Travice.view.edit.Maintenances', {
    extend: 'Travice.view.GridPanel',
    xtype: 'maintenancesView',

    requires: [
        'Travice.view.edit.MaintenancesController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'maintenances',
    store: 'Maintenances',

    tbar: {
        xtype: 'editToolbar'
    },

    listeners: {
        selectionchange: 'onSelectionChange'
    },

    columns: {
        defaults: {
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal
        },
        items: [{
            text: Strings.sharedName,
            dataIndex: 'name',
            filter: 'string'
        }, {
            text: Strings.sharedType,
            dataIndex: 'type',
            filter: {
                type: 'list',
                idField: 'key',
                labelField: 'name',
                store: 'MaintenanceTypes'
            },
            renderer: function (value) {
                var attribute = Ext.getStore('MaintenanceTypes').getById(value);
                return attribute && attribute.get('name') || value;
            }
        }, {
            text: Strings.maintenanceStart,
            dataIndex: 'start',
            renderer: function (value, metaData, record) {
                return Travice.AttributeFormatter.renderAttribute(
                    value, Ext.getStore('MaintenanceTypes').getById(record.get('type')));
            }
        }, {
            text: Strings.maintenancePeriod,
            dataIndex: 'period',
            renderer: function (value, metaData, record) {
                return Travice.AttributeFormatter.renderAttribute(
                    value, Ext.getStore('MaintenanceTypes').getById(record.get('type')));
            }
        }]
    }
});
