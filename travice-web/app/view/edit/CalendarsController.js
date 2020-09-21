

Ext.define('Travice.view.edit.CalendarsController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.calendars',

    requires: [
        'Travice.view.dialog.Calendar',
        'Travice.model.Calendar'
    ],

    objectModel: 'Travice.model.Calendar',
    objectDialog: 'Travice.view.dialog.Calendar',
    removeTitle: Strings.sharedCalendar
});
