

Ext.define('Travice.view.permissions.Calendars', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkCalendarsView',

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
