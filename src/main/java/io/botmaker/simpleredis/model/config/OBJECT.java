package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.ObjectProperty;

public final class OBJECT<T> extends AbstractPropertyBuilder<ObjectProperty, T> {

    private static final long serialVersionUID = -2702019041291004750L;

    public OBJECT(final RedisEntity owner, final Class<T> itemClass) {
        super(new ObjectProperty<>(owner, itemClass), null);
    }
}
