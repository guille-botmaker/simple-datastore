package com.zupcat.property;

import com.zupcat.model.PersistentObject;

import java.io.Serializable;

public class ListStringProperty extends AbstractListAnyProperty<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public ListStringProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, sentToClient, auditable);
    }

    @Override
    protected String convertItemFromString(final String s) {
        return s;
    }
}
