package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

public final class IntegerProperty extends PropertyMeta<Integer> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public IntegerProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected Integer getValueImpl(final DataObject dataObject) {
        return dataObject.has(name) ? dataObject.getInt(name) : null;
    }

    protected void setValueImpl(final Integer value, final DataObject dataObject) {
        dataObject.put(name, value);
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
