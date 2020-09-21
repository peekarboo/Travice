

Ext.define('Travice.view.edit.Notifications', {
    extend: 'Travice.view.GridPanel',
    xtype: 'notificationsView',

    requires: [
        'Travice.view.edit.NotificationsController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'notifications',
    store: 'Notifications',

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
            text: Strings.notificationType,
            dataIndex: 'type',
            flex: 2,
            renderer: function (value) {
                return Travice.app.getEventString(value);
            },
            filter: {
                type: 'list',
                idField: 'type',
                labelField: 'name',
                store: 'AllNotificationTypes'
            }
        }, {
            text: Strings.notificationAlways,
            dataIndex: 'always',
            renderer: Travice.AttributeFormatter.getFormatter('always'),
            filter: 'boolean'
        }, {
            text: Strings.notificationNotificators,
            dataIndex: 'notificators',
            flex: 2,
            filter: {
                type: 'arraylist',
                idField: 'type',
                labelField: 'name',
                store: 'AllNotificators'
            },
            renderer: function (value) {
                var result = '', i, notificators;
                if (value) {
                    notificators = value.split(/[ ,]+/).filter(Boolean);
                    for (i = 0; i < notificators.length; i++) {
                        result += Travice.app.getNotificatorString(notificators[i]) + (i < notificators.length - 1 ? ', ' : '');
                    }
                }
                return result;
            }
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
