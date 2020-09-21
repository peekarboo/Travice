

Ext.define('Travice.view.permissions.Geofences', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkGeofencesView',

    columns: {
        items: [{
            text: Strings.sharedName,
            dataIndex: 'name',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'string'
        }, {
            text: Strings.sharedCalendar,
            dataIndex: 'calendarId',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            hidden: true,
            filter: {
                type: 'list',
                labelField: 'name',
                store: 'AllCalendars'
            },
            renderer: Travice.AttributeFormatter.getFormatter('calendarId')
        }]
    }
});
