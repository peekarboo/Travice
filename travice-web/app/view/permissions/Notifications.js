

Ext.define('Travice.view.permissions.Notifications', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkNotificationsView',

    columns: {
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
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
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
