package com.zupcat.property;

import com.zupcat.model.PersistentObject;

import java.io.Serializable;

public class MapStringStringProperty extends AbstractMapStringAnyProperty<String, String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapStringStringProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, sentToClient, auditable);
    }

    @Override
    protected String convertKeyFromString(final String s) {
        return s;
    }

    @Override
    protected String convertValueFromString(final String s) {
        return s;
    }
}
