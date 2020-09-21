

Ext.define('Travice.store.AllGroups', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.Group',

    proxy: {
        type: 'rest',
        url: 'api/groups',
        extraParams: {
            all: true
        }
    }
});
