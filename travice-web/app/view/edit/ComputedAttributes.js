

Ext.define('Travice.view.edit.ComputedAttributes', {
    extend: 'Travice.view.GridPanel',
    xtype: 'computedAttributesView',

    requires: [
        'Travice.view.edit.ComputedAttributesController',
        'Travice.view.edit.Toolbar'
    ],

    controller: 'computedAttributes',
    store: 'ComputedAttributes',

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
            text: Strings.sharedDescription,
            dataIndex: 'description',
            filter: 'string'
        }, {
            text: Strings.sharedAttribute,
            dataIndex: 'attribute',
            filter: {
                type: 'list',
                labelField: 'name',
                store: 'PositionAttributes'
            },
            renderer: function (value) {
                return Ext.getStore('PositionAttributes').getAttributeName(value);
            }
        }, {
            text: Strings.sharedExpression,
            dataIndex: 'expression'
        }, {
            text: Strings.sharedType,
            dataIndex: 'type',
            filter: {
                type: 'list',
                labelField: 'name',
                store: 'AttributeValueTypes'
            },
            renderer: function (value) {
                var type = Ext.getStore('AttributeValueTypes').getById(value);
                if (type) {
                    return type.get('name');
                } else {
                    return value;
                }
            }
        }]
    }
});
