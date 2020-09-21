

Ext.define('Travice.view.BaseWindow', {
    extend: 'Ext.window.Window',

    width: Travice.Style.windowWidth,
    height: Travice.Style.windowHeight,
    layout: 'fit',

    initComponent: function () {
        if (window.innerWidth < Travice.Style.windowWidth || window.innerHeight < Travice.Style.windowHeight) {
            this.maximized = true;
            this.style = 'border-width: 0';
        }
        this.callParent();
    }
});
