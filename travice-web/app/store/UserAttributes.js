
Ext.define('Travice.store.UserAttributes', {
    extend: 'Ext.data.Store',
    model: 'Travice.model.KnownAttribute',

    data: [{
        key: 'mail.smtp.host',
        name: Strings.attributeMailSmtpHost,
        valueType: 'string'
    }, {
        key: 'mail.smtp.port',
        name: Strings.attributeMailSmtpPort,
        valueType: 'number',
        allowDecimals: false,
        minValue: 1,
        maxValue: 65535
    }, {
        key: 'mail.smtp.starttls.enable',
        name: Strings.attributeMailSmtpStarttlsEnable,
        valueType: 'boolean'
    }, {
        key: 'mail.smtp.starttls.required',
        name: Strings.attributeMailSmtpStarttlsRequired,
        valueType: 'boolean'
    }, {
        key: 'mail.smtp.ssl.enable',
        name: Strings.attributeMailSmtpSslEnable,
        valueType: 'boolean'
    }, {
        key: 'mail.smtp.ssl.trust',
        name: Strings.attributeMailSmtpSslTrust,
        valueType: 'string'
    }, {
        key: 'mail.smtp.ssl.protocols',
        name: Strings.attributeMailSmtpSslProtocols,
        valueType: 'string'
    }, {
        key: 'mail.smtp.from',
        name: Strings.attributeMailSmtpFrom,
        valueType: 'string'
    }, {
        key: 'mail.smtp.auth',
        name: Strings.attributeMailSmtpAuth,
        valueType: 'boolean'
    }, {
        key: 'mail.smtp.username',
        name: Strings.attributeMailSmtpUsername,
        valueType: 'string'
    }, {
        key: 'mail.smtp.password',
        name: Strings.attributeMailSmtpPassword,
        valueType: 'string'
    }]
});
