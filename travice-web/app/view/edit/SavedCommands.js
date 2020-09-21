

Ext.define('Travice.view.edit.SavedCommands', {
    extend: 'Travice.view.GridPanel',
    xtype: 'savedCommandsView',

    requires: [
        'Travice.view.edit.SavedCommandsController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'savedCommands',
    store: 'Commands',

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
            text: Strings.sharedDescription,
            dataIndex: 'description',
            filter: 'string'
        }, {
            text: Strings.sharedType,
            dataIndex: 'type',
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
            renderer: Travice.AttributeFormatter.getFormatter('textChannel'),
            filter: 'boolean'
        }]
    }
});
