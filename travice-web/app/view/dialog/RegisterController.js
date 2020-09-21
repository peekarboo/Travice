

Ext.define('Travice.view.dialog.RegisterController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.register',

    onCreateClick: function () {
        var form = this.lookupReference('form');
        if (form.isValid()) {
            Ext.Ajax.request({
                scope: this,
                method: 'POST',
                url: 'api/users',
                jsonData: form.getValues(),
                callback: this.onCreateReturn
            });
        }
    },

    onCreateReturn: function (options, success, response) {
        if (success) {
            this.closeView();
            Travice.app.showToast(Strings.loginCreated);
        } else {
            Travice.app.showError(response);
        }
    }

});
