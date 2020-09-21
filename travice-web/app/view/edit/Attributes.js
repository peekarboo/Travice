

Ext.define('Travice.view.edit.Attributes', {
    extend: 'Travice.view.GridPanel',
    xtype: 'attributesView',

    requires: [
        'Travice.view.edit.AttributesController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'attributes',

    tbar: {
        xtype: 'editToolbar'
    },

    listeners: {
        selectionchange: 'onSelectionChange'
    },

    columns: {
        defaults: {
            flex: 1,
            minWidth: Travice.Style.columnWidthNormal
        },
        items: [{
            text: Strings.sharedName,
            dataIndex: 'name',
            filter: 'string',
            renderer: function (value) {
                var attribute;
                if (this.attributesStore) {
                    attribute = Ext.getStore(this.attributesStore).getById(value);
                }
                return attribute && attribute.get('name') || value;
            }
        }, {
            text: Strings.stateValue,
            dataIndex: 'value',
            renderer: function (value, metaData, record) {
                var attribute;
                if (this.attributesStore) {
                    attribute = Ext.getStore(this.attributesStore).getById(record.get('name'));
                }
                return Travice.AttributeFormatter.renderAttribute(value, attribute);
            }
        }]
    }
});
