

Ext.define('Travice.view.dialog.ReportConfig', {
    extend: 'Travice.view.dialog.Base',

    requires: [
        'Travice.view.dialog.ReportConfigController',
        'Travice.view.CustomTimeField'
    ],

    controller: 'reportConfig',
    title: Strings.reportConfigure,

    items: [{
        fieldLabel: Strings.reportDevice,
        xtype: 'tagfield',
        reference: 'deviceField',
        maxWidth: Travice.Style.formFieldWidth,
        store: 'Devices',
        valueField: 'id',
        displayField: 'name',
        queryMode: 'local'
    }, {
        fieldLabel: Strings.reportGroup,
        xtype: 'tagfield',
        reference: 'groupField',
        maxWidth: Travice.Style.formFieldWidth,
        store: 'Groups',
        valueField: 'id',
        displayField: 'name',
        queryMode: 'local'
    }, {
        fieldLabel: Strings.reportEventTypes,
        xtype: 'tagfield',
        reference: 'eventTypeField',
        maxWidth: Travice.Style.formFieldWidth,
        store: 'ReportEventTypes',
        hidden: true,
        valueField: 'type',
        displayField: 'name',
        queryMode: 'local'
    }, {
        fieldLabel: Strings.reportChartType,
        xtype: 'combobox',
        reference: 'chartTypeField',
        store: 'ReportChartTypes',
        hidden: true,
        value: 'speed',
        valueField: 'key',
        displayField: 'name',
        queryMode: 'local'
    }, {
        fieldLabel: Strings.reportShowMarkers,
        xtype: 'checkbox',
        reference: 'showMarkersField',
        inputValue: true,
        uncheckedValue: false,
        value: false
    }, {
        fieldLabel: Strings.reportPeriod,
        reference: 'periodField',
        xtype: 'combobox',
        store: 'ReportPeriods',
        editable: false,
        valueField: 'key',
        displayField: 'name',
        queryMode: 'local',
        listeners: {
            change: 'onPeriodChange'
        }
    }, {
        xtype: 'fieldcontainer',
        layout: 'vbox',
        reference: 'fromContainer',
        hidden: true,
        fieldLabel: Strings.reportFrom,
        items: [{
            xtype: 'datefield',
            reference: 'fromDateField',
            startDay: Travice.Style.weekStartDay,
            format: Travice.Style.dateFormat,
            value: new Date(new Date().getTime() - 30 * 60 * 1000)
        }, {
            xtype: 'customTimeField',
            reference: 'fromTimeField',
            value: new Date(new Date().getTime() - 30 * 60 * 1000)
        }]
    }, {
        xtype: 'fieldcontainer',
        layout: 'vbox',
        reference: 'toContainer',
        hidden: true,
        fieldLabel: Strings.reportTo,
        items: [{
            xtype: 'datefield',
            reference: 'toDateField',
            startDay: Travice.Style.weekStartDay,
            format: Travice.Style.dateFormat,
            value: new Date()
        }, {
            xtype: 'customTimeField',
            reference: 'toTimeField',
            value: new Date()
        }]
    }],

    buttons: [{
        glyph: 'xf00c@FontAwesome',
        tooltip: Strings.sharedSave,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'onSaveClick'
    }, {
        glyph: 'xf00d@FontAwesome',
        tooltip: Strings.sharedCancel,
        tooltipType: 'title',
        minWidth: 0,
        handler: 'closeView'
    }]
});
