package com.zupcat.property;

import com.zupcat.model.PersistentObject;

import java.io.Serializable;

public class ListIntegerProperty extends AbstractListAnyProperty<Integer> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public ListIntegerProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, sentToClient, auditable);
    }

    @Override
    protected Integer convertItemFromString(final String s) {
        return s == null ? null : Integer.valueOf(s);
    }
}
