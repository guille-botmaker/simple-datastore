package com.zupcat.property;

import com.zupcat.model.PersistentObject;

import java.io.Serializable;

public class MapIntegerStringProperty extends AbstractMapStringAnyProperty<Integer, String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapIntegerStringProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, sentToClient, auditable);
    }

    @Override
    protected Integer convertKeyFromString(final String s) {
        return s == null ? null : Integer.valueOf(s);
    }

    @Override
    protected String convertValueFromString(final String s) {
        return s;
    }
}
