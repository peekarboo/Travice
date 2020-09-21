

Ext.define('Travice.view.edit.ComputedAttributesController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.computedAttributes',

    requires: [
        'Travice.view.dialog.ComputedAttribute',
        'Travice.model.ComputedAttribute'
    ],

    objectModel: 'Travice.model.ComputedAttribute',
    objectDialog: 'Travice.view.dialog.ComputedAttribute',
    removeTitle: Strings.sharedComputedAttribute
});
