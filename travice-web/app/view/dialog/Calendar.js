

Ext.define('Travice.view.dialog.Calendar', {
    extend: 'Travice.view.dialog.BaseEdit',

    requires: [
        'Travice.view.dialog.CalendarController'
    ],

    controller: 'calendar',
    title: Strings.sharedCalendar,

    items: {
        xtype: 'form',
        items: [{
            xtype: 'fieldset',
            title: Strings.sharedRequired,
            items: [{
                xtype: 'textfield',
                name: 'name',
                fieldLabel: Strings.sharedName,
                allowBlank: false
            }, {
                xtype: 'filefield',
                name: 'file',
                fieldLabel: Strings.sharedFile,
                allowBlank: false,
                buttonConfig: {
                    glyph: 'xf093@FontAwesome',
                    text: '',
                    tooltip: Strings.sharedSelectFile,
                    tooltipType: 'title',
                    minWidth: 0
                },
                listeners: {
                    change: 'onFileChange'
                }
            }]
        }, {
            xtype: 'hiddenfield',
            name: 'data',
            allowBlank: false,
            reference: 'dataField'
        }]
    }
});
