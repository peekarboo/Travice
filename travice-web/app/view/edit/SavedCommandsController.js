

Ext.define('Travice.view.edit.SavedCommandsController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.savedCommands',

    requires: [
        'Travice.view.dialog.SavedCommand',
        'Travice.model.Command'
    ],

    objectModel: 'Travice.model.Command',
    objectDialog: 'Travice.view.dialog.SavedCommand',
    removeTitle: Strings.sharedSavedCommand
});
