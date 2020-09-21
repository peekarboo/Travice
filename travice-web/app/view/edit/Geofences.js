

Ext.define('Travice.view.edit.Geofences', {
    extend: 'Travice.view.GridPanel',
    xtype: 'geofencesView',

    requires: [
        'Travice.view.edit.GeofencesController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'geofences',
    store: 'Geofences',

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
            dataIndex: 'description',
            filter: 'string'
        }, {
            text: Strings.sharedCalendar,
            dataIndex: 'calendarId',
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
