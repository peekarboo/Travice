

Ext.define('Travice.view.permissions.SavedCommands', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkSavedCommandsView',

    columns: {
        items: [{
            text: Strings.sharedDescription,
            dataIndex: 'description',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'string'
        }, {
            text: Strings.sharedType,
            dataIndex: 'type',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: {
                type: 'list',
                idField: 'type',
                labelField: 'name',
                store: 'AllCommandTypes'
            },
            renderer: Travice.AttributeFormatter.getFormatter('commandType')
        }, {
            text: Strings.notificationSms,
            dataIndex: 'textChannel',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'boolean'
        }]
    }
});
