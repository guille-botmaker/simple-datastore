package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;

public class IntegerProperty extends PropertyMeta<Integer> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public IntegerProperty(final DatastoreEntity owner, final Integer initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable) {
        super(owner, initialValue, sentToClient, auditable, indexable);
    }

    protected Integer getValueImpl(final ObjectVar objectVar) {
        return objectVar.getInteger(name);
    }

    protected void setValueImpl(final Integer value, final ObjectVar objectVar) {
        objectVar.set(name, value);
    }

    public void add(final int value) {
        final Integer current = get();
        this.set(current == null ? value : (current + value));
    }

    public void substract(final int value) {
        this.add(-value);
    }

    public void decrement() {
        this.substract(1);
    }

    public void increment() {
        this.add(1);
    }

    public boolean isNullOrZero() {
        return this.get() == null || this.get() == 0;
    }
}
