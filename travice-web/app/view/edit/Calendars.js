

Ext.define('Travice.view.edit.Calendars', {
    extend: 'Travice.view.GridPanel',
    xtype: 'calendarsView',

    requires: [
        'Travice.view.edit.CalendarsController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'calendars',
    store: 'Calendars',

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
        }]
    }
});
