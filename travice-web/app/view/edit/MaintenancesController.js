

Ext.define('Travice.view.edit.MaintenancesController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.maintenances',

    requires: [
        'Travice.view.dialog.Maintenance',
        'Travice.model.Maintenance'
    ],

    objectModel: 'Travice.model.Maintenance',
    objectDialog: 'Travice.view.dialog.Maintenance',
    removeTitle: Strings.sharedMaintenance
});
