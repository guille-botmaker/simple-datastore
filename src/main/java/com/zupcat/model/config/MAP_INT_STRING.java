package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapIntegerStringProperty;

public final class MAP_INT_STRING extends AbstractPropertyBuilder<MapIntegerStringProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP_INT_STRING(final DatastoreEntity owner) {
        super(new MapIntegerStringProperty(owner));
    }
}
