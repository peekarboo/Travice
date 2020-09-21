
Ext.define('Travice.view.MainController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.mainController',

    init: function () {
        this.lookupReference('reportView').setHidden(Travice.app.getBooleanAttributePreference('ui.disableReport'));
        this.lookupReference('eventsView').setHidden(Travice.app.getBooleanAttributePreference('ui.disableEvents'));
    }
});
