package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

public final class StringProperty extends PropertyMeta<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public StringProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected String getValueImpl(final DataObject dataObject) {
        return dataObject.has(name) ? dataObject.getString(name) : null;
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
