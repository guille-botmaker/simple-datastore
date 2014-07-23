package com.zupcat.property;

import com.zupcat.model.ObjectVar;
import com.zupcat.model.PersistentObject;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;

public class LongProperty extends PropertyMeta<Long> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public LongProperty(final PersistentObject owner, final String name, final Long initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable) {
        super(owner, name, initialValue, sentToClient, auditable, indexable);
    }

    protected Long getValueImpl(final ObjectVar objectVar) {
        return objectVar.getLong(name);
    }

    protected void setValueImpl(final Long value, final ObjectVar objectVar) {
        objectVar.set(name, value);
    }
}
