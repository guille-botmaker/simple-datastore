package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ComplexAnyProperty;

public final class COMPLEX_ANY<T> extends AbstractPropertyBuilder<ComplexAnyProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;


    public COMPLEX_ANY(final DatastoreEntity owner) {
        super(new ComplexAnyProperty(owner), null);
    }
}
