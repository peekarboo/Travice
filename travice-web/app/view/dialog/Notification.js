

Ext.define('Travice.view.dialog.Notification', {
    extend: 'Travice.view.dialog.BaseEdit',

    requires: [
        'Travice.view.ClearableComboBox',
        'Travice.view.dialog.NotificationController'
    ],

    controller: 'notification',
    title: Strings.sharedNotification,

    items: {
        xtype: 'form',
        items: [{
            xtype: 'fieldset',
            title: Strings.sharedRequired,
            items: [{
                xtype: 'combobox',
                name: 'type',
                fieldLabel: Strings.sharedType,
                store: 'AllNotificationTypes',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'type',
                editable: false,
                allowBlank: false
            }, {
                xtype: 'checkboxfield',
                inputValue: true,
                uncheckedValue: false,
                name: 'always',
                fieldLabel: Strings.notificationAlways
            }, {
                fieldLabel: Strings.notificationNotificators,
                xtype: 'tagfield',
                name: 'notificators',
                maxWidth: Travice.Style.formFieldWidth,
                store: 'AllNotificators',
                valueField: 'type',
                displayField: 'name',
                queryMode: 'local'
            }]
        }, {
            xtype: 'fieldset',
            title: Strings.sharedExtra,
            collapsible: true,
            collapsed: true,
            items: [{
                xtype: 'clearableComboBox',
                reference: 'calendarCombo',
                name: 'calendarId',
                store: 'Calendars',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                fieldLabel: Strings.sharedCalendar
            }]
        }]
    }
});
