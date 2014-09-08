package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ListIntegerProperty;

public final class LIST_INTEGER extends AbstractPropertyBuilder<ListIntegerProperty, Integer> {

    private static final long serialVersionUID = -2702019046191004750L;


    public LIST_INTEGER(final DatastoreEntity owner) {
        super(new ListIntegerProperty(owner));
    }
}
