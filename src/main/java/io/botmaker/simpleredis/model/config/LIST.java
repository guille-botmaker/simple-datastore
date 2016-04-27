package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.ListProperty;

public final class LIST<T> extends AbstractPropertyBuilder<ListProperty<T>, T> {

    private static final long serialVersionUID = -2702019046191004750L;

    public LIST(final RedisEntity owner) {
        this(owner, null);
    }

    public LIST(final RedisEntity owner, final Class<? extends DataObject> itemClass) {
        this(owner, itemClass, false);
    }

    public LIST(final RedisEntity owner, final Class<? extends DataObject> itemClass, final boolean keepUniqueElements) {
        super(new ListProperty<>(owner, itemClass, keepUniqueElements), null);
    }
}
