package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.StringProperty;

public final class STRING extends AbstractPropertyBuilder<StringProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;

    public STRING(final RedisEntity owner) {
        super(new StringProperty(owner), null);
    }
}
