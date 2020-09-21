

Ext.define('Travice.view.permissions.ComputedAttributes', {
    extend: 'Travice.view.permissions.Base',
    xtype: 'linkComputedAttributesView',

    columns: {
        items: [{
            text: Strings.sharedDescription,
            dataIndex: 'description',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: 'string'
        }, {
            text: Strings.sharedAttribute,
            dataIndex: 'attribute',
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal,
            filter: {
                type: 'list',
                labelField: 'name',
                store: 'PositionAttributes'
            },
            renderer: function (value) {
                return Ext.getStore('PositionAttributes').getAttributeName(value);
            }
        }]
    }
});
