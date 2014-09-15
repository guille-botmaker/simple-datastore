package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapStringLongProperty;

public final class MAP_STRING_LONG extends AbstractPropertyBuilder<MapStringLongProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP_STRING_LONG(final DatastoreEntity owner) {
        super(new MapStringLongProperty(owner), null);
    }
}
