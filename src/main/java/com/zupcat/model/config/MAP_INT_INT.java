package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapIntegerIntegerProperty;

public final class MAP_INT_INT extends AbstractPropertyBuilder<MapIntegerIntegerProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP_INT_INT(final DatastoreEntity owner) {
        super(new MapIntegerIntegerProperty(owner));
    }
}
