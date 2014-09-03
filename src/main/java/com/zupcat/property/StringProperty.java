package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;

public class StringProperty extends PropertyMeta<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private final boolean toLowerCase;

    public StringProperty(final DatastoreEntity owner, final String initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable, final boolean toLowerCase) {
        super(owner, initialValue, sentToClient, auditable, indexable);

        this.toLowerCase = toLowerCase;
    }

    protected String getValueImpl(final ObjectVar objectVar) {
        return objectVar.getString(name);
    }

    protected void setValueImpl(final String value, final ObjectVar objectVar) {
        objectVar.set(name, (value == null ? null : (toLowerCase ? value.toLowerCase() : value)));
    }
}
