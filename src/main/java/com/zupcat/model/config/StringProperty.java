package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;

import java.io.Serializable;

public class StringProperty extends PropertyMeta<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public StringProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected String getValueImpl(final ObjectVar objectVar) {
        return objectVar.getString(name);
    }

    protected void setValueImpl(final String value, final ObjectVar objectVar) {
        objectVar.set(name, (value == null ? null : (options.toLowerCase ? value.toLowerCase() : value)));
    }

    public boolean hasData() {
        final String s = this.get();

        return s != null && s.trim().length() > 0;
    }
}
