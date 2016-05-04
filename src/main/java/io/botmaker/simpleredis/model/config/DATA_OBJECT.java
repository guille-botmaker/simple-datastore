package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.DataObjectProperty;

public final class DATA_OBJECT<T extends DataObject> extends AbstractPropertyBuilder<DataObjectProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;

    public DATA_OBJECT(final RedisEntity owner, final Class<T> itemClass) {
        super(new DataObjectProperty(owner, itemClass), null);
    }
}
