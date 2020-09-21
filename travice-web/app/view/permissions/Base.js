
Ext.define('Travice.view.permissions.Base', {
    extend: 'Travice.view.GridPanel',

    requires: [
        'Travice.view.permissions.BaseController'
    ],

    controller: 'base',

    selModel: {
        selType: 'checkboxmodel',
        checkOnly: true,
        showHeaderCheckbox: false
    },

    listeners: {
        beforedeselect: 'onBeforeDeselect',
        beforeselect: 'onBeforeSelect'
    }
});
