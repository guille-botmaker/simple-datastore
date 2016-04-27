package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.DatastoreEntity;
import io.botmaker.simpleredis.property.MapProperty;

public final class MAP<T> extends AbstractPropertyBuilder<MapProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;

    public MAP(final DatastoreEntity owner) {
        this(owner, null);
    }

    public MAP(final DatastoreEntity owner, final Class<? extends DataObject> valueClass) {
        super(new MapProperty(owner, valueClass), null);
    }
}
