package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

public class LongProperty extends PropertyMeta<Long> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public LongProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected Long getValueImpl(final DataObject dataObject) {
        return dataObject.getLong(name);
    }

    protected void setValueImpl(final Long value, final DataObject dataObject) {
        dataObject.put(name, value);
    }

    public void add(final long value) {
        final Long current = get();
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
        return this.get() == null || this.get() == 0l;
    }
}
