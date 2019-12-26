package io.botmaker.simpleredis.property;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;

import java.io.Serializable;

public class StringProperty extends PropertyMeta<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public StringProperty(final RedisEntity owner) {
        super(owner);
    }

    protected String getValueImpl(final DataObject dataObject) {
        return dataObject.optString(name, null);
    }

    protected void setValueImpl(final String value, final DataObject dataObject) {
        dataObject.put(name, (value == null ? null : (options.stringIsOnlyLowerCase ? value.toLowerCase() : value)));
    }

    @Override
    public void setFromStringValue(final String stringValue, final boolean forceAudit) {
        set(stringValue, forceAudit);
    }

    public boolean hasData() {
        final String s = this.get();

        return s != null && s.trim().length() > 0;
    }
}
