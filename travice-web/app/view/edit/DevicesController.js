

Ext.define('Travice.view.edit.DevicesController', {
    extend: 'Travice.view.edit.ToolbarController',
    alias: 'controller.devices',

    requires: [
        'Travice.view.dialog.SendCommand',
        'Travice.view.dialog.Device',
        'Travice.view.permissions.Geofences',
        'Travice.view.permissions.ComputedAttributes',
        'Travice.view.permissions.Drivers',
        'Travice.view.permissions.SavedCommands',
        'Travice.view.BaseWindow',
        'Travice.model.Device',
        'Travice.model.Command'
    ],

    config: {
        listen: {
            controller: {
                '*': {
                    selectreport: 'selectReport'
                },
                'root': {
                    selectdevice: 'selectDevice'
                },
                'map': {
                    selectdevice: 'selectDevice',
                    deselectfeature: 'deselectFeature'
                }
            },
            store: {
                '#Devices': {
                    update: 'onUpdateDevice'
                }
            }
        }
    },

    objectModel: 'Travice.model.Device',
    objectDialog: 'Travice.view.dialog.Device',
    removeTitle: Strings.sharedDevice,

    init: function () {
        var self = this, readonly, deviceReadonly;
        deviceReadonly = Travice.app.getPreference('deviceReadonly', false) && !Travice.app.getUser().get('administrator');
        readonly = Travice.app.getPreference('readonly', false) && !Travice.app.getUser().get('administrator');
        this.lookupReference('toolbarAddButton').setDisabled(readonly || deviceReadonly);
        this.lookupReference('toolbarDeviceMenu').setHidden(readonly || deviceReadonly);

        setInterval(function () {
            self.getView().getView().refresh();
        }, Travice.Style.refreshPeriod);
    },

    onCommandClick: function () {
        var device, deviceId, dialog, typesStore, commandsStore;
        device = this.getView().getSelectionModel().getSelection()[0];
        deviceId = device.get('id');

        dialog = Ext.create('Travice.view.dialog.SendCommand');
        dialog.deviceId = deviceId;

        commandsStore = dialog.lookupReference('commandsComboBox').getStore();
        commandsStore.getProxy().setExtraParam('deviceId', deviceId);
        if (!Travice.app.getPreference('limitCommands', false)) {
            commandsStore.add({
                id: 0,
                description: Strings.sharedNew
            });
        }
        commandsStore.load({
            addRecords: true
        });

        typesStore = dialog.lookupReference('commandType').getStore();
        typesStore.getProxy().setExtraParam('deviceId', deviceId);
        typesStore.load();

        dialog.show();
    },

    updateButtons: function (selected) {
        var readonly, deviceReadonly, empty, deviceMenu;
        deviceReadonly = Travice.app.getPreference('deviceReadonly', false) && !Travice.app.getUser().get('administrator');
        readonly = Travice.app.getPreference('readonly', false) && !Travice.app.getUser().get('administrator');
        empty = selected.length === 0;
        this.lookupReference('toolbarEditButton').setDisabled(empty || readonly || deviceReadonly);
        this.lookupReference('toolbarRemoveButton').setDisabled(empty || readonly || deviceReadonly);
        deviceMenu = this.lookupReference('toolbarDeviceMenu');
        deviceMenu.device = empty ? null : selected[0];
        deviceMenu.setDisabled(empty);
        this.lookupReference('deviceCommandButton').setDisabled(empty || readonly);
    },

    onSelectionChange: function (selection, selected) {
        this.updateButtons(selected);
        if (selected.length > 0) {
            this.fireEvent('selectdevice', selected[0], true);
        } else {
            this.fireEvent('deselectfeature');
        }
    },

    selectDevice: function (device) {
        this.getView().getSelectionModel().select([device], false, true);
        this.updateButtons(this.getView().getSelectionModel().getSelected().items);
        this.getView().getView().focusRow(device);
    },

    selectReport: function (position) {
        if (position !== undefined) {
            this.deselectFeature();
        }
    },

    onUpdateDevice: function () {
        this.updateButtons(this.getView().getSelectionModel().getSelected().items);
    },

    deselectFeature: function () {
        this.getView().getSelectionModel().deselectAll();
    }
});
