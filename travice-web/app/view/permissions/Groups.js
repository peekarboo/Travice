

Ext.define('Travice.view.permissions.Groups', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkGroupsView',

    requires: [
        'Travice.AttributeFormatter'
    ],

    columns: {
        items: [{
            text: Strings.sharedName,
            dataIndex: 'name',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'string'
        }, {
            text: Strings.groupDialog,
            dataIndex: 'groupId',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            hidden: true,
            filter: {
                type: 'list',
                labelField: 'name',
                store: 'AllGroups'
            },
            renderer: Travice.AttributeFormatter.getFormatter('groupId')
        }]
    }
});
