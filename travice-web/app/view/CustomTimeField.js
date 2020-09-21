

Ext.define('Travice.view.CustomTimeField', {
    extend: 'Ext.form.field.Time',
    xtype: 'customTimeField',

    constructor: function (config) {
        if (Travice.app.getPreference('twelveHourFormat', false)) {
            config.format = Travice.Style.timeFormat12;
        } else {
            config.format = Travice.Style.timeFormat24;
        }
        this.callParent(arguments);
    }
});
