package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.DateProperty;

public final class DATE extends AbstractPropertyBuilder<DateProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;

    public DATE(final RedisEntity owner) {
        super(new DateProperty(owner), "0");
    }
}
