

Ext.define('Travice.view.permissions.Drivers', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkDriversView',

    columns: {
        items: [{
            text: Strings.sharedName,
            dataIndex: 'name',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'string'
        }, {
            text: Strings.deviceIdentifier,
            dataIndex: 'uniqueId',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'string'
        }]
    }
});
