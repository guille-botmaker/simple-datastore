package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapProperty;

public final class MAP<T> extends AbstractPropertyBuilder<MapProperty, T> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP(final DatastoreEntity owner) {
        super(new MapProperty(owner), null);
    }
}
