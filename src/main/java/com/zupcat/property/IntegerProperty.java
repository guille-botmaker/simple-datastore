package com.zupcat.property;

import com.zupcat.model.ObjectVar;
import com.zupcat.model.PersistentObject;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;

public class IntegerProperty extends PropertyMeta<Integer> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public IntegerProperty(final PersistentObject owner, final String name, final Integer initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable) {
        super(owner, name, initialValue, sentToClient, auditable, indexable);
    }

    protected Integer getValueImpl(final ObjectVar objectVar) {
        return objectVar.getInteger(name);
    }

    protected void setValueImpl(final Integer value, final ObjectVar objectVar) {
        objectVar.set(name, value);
    }
}
