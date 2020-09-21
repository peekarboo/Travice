

Ext.define('Travice.view.permissions.Users', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkUsersView',

    columns: {
        items: [{
            text: Strings.sharedName,
            dataIndex: 'name',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'string'
        }]
    }
});
