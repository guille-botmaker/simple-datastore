package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.IntegerProperty;

public final class INT extends AbstractPropertyBuilder<IntegerProperty, Integer> {

    private static final long serialVersionUID = -2702019046191004750L;

    public INT(final RedisEntity owner) {
        super(new IntegerProperty(owner), 0);
    }
}