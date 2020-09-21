

Ext.define('Travice.view.edit.NotificationsController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.notifications',

    requires: [
        'Travice.view.dialog.Notification',
        'Travice.model.Notification'
    ],

    objectModel: 'Travice.model.Notification',
    objectDialog: 'Travice.view.dialog.Notification',
    removeTitle: Strings.sharedNotification
});
