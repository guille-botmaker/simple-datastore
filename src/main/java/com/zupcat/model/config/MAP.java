package com.zupcat.model.config;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapProperty;

public final class MAP<T> extends AbstractPropertyBuilder<MapProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP(final DatastoreEntity owner) {
        this(owner, null);
    }

    public MAP(final DatastoreEntity owner, final Class<? extends DataObject> valueClass) {
        super(new MapProperty(owner, valueClass), null);
    }
}
