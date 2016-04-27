package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.LongProperty;

public final class LONG extends AbstractPropertyBuilder<LongProperty, Long> {

    private static final long serialVersionUID = -2702019046191004750L;

    public LONG(final RedisEntity owner) {
        super(new LongProperty(owner), 0L);
    }
}
