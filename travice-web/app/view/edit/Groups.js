

Ext.define('Travice.view.edit.Groups', {
    extend: 'Travice.view.GridPanel',
    xtype: 'groupsView',

    requires: [
        'Travice.AttributeFormatter',
        'Travice.view.edit.GroupsController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'groups',
    store: 'Groups',

    tbar: {
        xtype: 'editToolbar',
        items: [{
            xtype: 'button',
            disabled: true,
            handler: 'onGeofencesClick',
            reference: 'toolbarGeofencesButton',
            glyph: 'xf21d@FontAwesome',
            tooltip: Strings.sharedGeofences,
            tooltipType: 'title'
        }, {
            xtype: 'button',
            disabled: true,
            handler: 'onAttributesClick',
            reference: 'toolbarAttributesButton',
            glyph: 'xf0ae@FontAwesome',
            tooltip: Strings.sharedComputedAttributes,
            tooltipType: 'title'
        }, {
            xtype: 'button',
            disabled: true,
            handler: 'onDriversClick',
            reference: 'toolbarDriversButton',
            glyph: 'xf2c2@FontAwesome',
            tooltip: Strings.sharedDrivers,
            tooltipType: 'title'
        }, {
            xtype: 'button',
            disabled: true,
            handler: 'onCommandsClick',
            reference: 'toolbarCommandsButton',
            glyph: 'xf093@FontAwesome',
            tooltip: Strings.sharedSavedCommands,
            tooltipType: 'title'
        }, {
            xtype: 'button',
            disabled: true,
            handler: 'onNotificationsClick',
            reference: 'toolbarNotificationsButton',
            glyph: 'xf003@FontAwesome',
            tooltip: Strings.sharedNotifications,
            tooltipType: 'title'
        }, {
            xtype: 'button',
            disabled: true,
            handler: 'onMaintenancesClick',
            reference: 'toolbarMaintenancesButton',
            glyph: 'xf0ad@FontAwesome',
            tooltip: Strings.sharedMaintenances,
            tooltipType: 'title'
        }]
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
            text: Strings.groupDialog,
            dataIndex: 'groupId',
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
