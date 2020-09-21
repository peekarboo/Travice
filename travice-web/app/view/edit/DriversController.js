

Ext.define('Travice.view.edit.DriversController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.drivers',

    requires: [
        'Travice.view.dialog.Driver',
        'Travice.model.Driver'
    ],

    objectModel: 'Travice.model.Driver',
    objectDialog: 'Travice.view.dialog.Driver',
    removeTitle: Strings.sharedDriver
});
