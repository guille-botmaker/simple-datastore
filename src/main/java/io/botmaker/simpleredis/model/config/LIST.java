package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.ListPrimitiveObjectProperty;

public final class LIST<T> extends AbstractPropertyBuilder<ListPrimitiveObjectProperty<T>, T> {

    private static final long serialVersionUID = -2702019046191004750L;

    public LIST(final RedisEntity owner) {
        this(owner, false);
    }

    public LIST(final RedisEntity owner, final boolean keepUniqueElements) {
        super(new ListPrimitiveObjectProperty<>(owner, keepUniqueElements), null);
    }
}
