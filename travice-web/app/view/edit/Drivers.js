

Ext.define('Travice.view.edit.Drivers', {
    extend: 'Travice.view.GridPanel',
    xtype: 'driversView',

    requires: [
        'Travice.view.edit.DriversController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'drivers',
    store: 'Drivers',

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
            text: Strings.sharedDescription,
            dataIndex: 'uniqueId',
            filter: 'string'
        }]
    }
});
