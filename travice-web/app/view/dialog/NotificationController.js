

Ext.define('Travice.view.dialog.NotificationController', {
    extend: 'Travice.view.dialog.BaseEditController',
    alias: 'controller.notification',

    init: function () {
        this.lookupReference('calendarCombo').setHidden(
            Travice.app.getBooleanAttributePreference('ui.disableCalendars'));
    }
});
