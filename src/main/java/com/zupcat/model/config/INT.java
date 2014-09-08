package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.IntegerProperty;

public final class INT extends AbstractPropertyBuilder<IntegerProperty, Integer> {

    private static final long serialVersionUID = -2702019046191004750L;


    public INT(final DatastoreEntity owner) {
        super(new IntegerProperty(owner));
    }
}
