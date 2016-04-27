package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.ComplexAnyProperty;

public final class COMPLEX_ANY<T> extends AbstractPropertyBuilder<ComplexAnyProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;

    public COMPLEX_ANY(final RedisEntity owner) {
        super(new ComplexAnyProperty(owner), null);
    }
}
