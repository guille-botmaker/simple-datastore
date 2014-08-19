package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;

public class LongProperty extends PropertyMeta<Long> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public LongProperty(final DatastoreEntity owner, final Long initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable) {
        super(owner, initialValue, sentToClient, auditable, indexable);
    }

    protected Long getValueImpl(final ObjectVar objectVar) {
        return objectVar.getLong(name);
    }

    protected void setValueImpl(final Long value, final ObjectVar objectVar) {
        objectVar.set(name, value);
    }
}
