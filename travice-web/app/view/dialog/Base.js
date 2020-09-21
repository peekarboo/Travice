

Ext.define('Travice.view.dialog.Base', {
    extend: 'Ext.window.Window',

    bodyPadding: Travice.Style.normalPadding,
    resizable: false,
    scrollable: true,
    constrain: true,

    initComponent: function () {
        if (window.innerHeight) {
            this.maxHeight = window.innerHeight - Travice.Style.normalPadding * 2;
        }
        this.callParent();
    }
});
