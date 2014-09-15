package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapStringIntegerProperty;

public final class MAP_STRING_INTEGER extends AbstractPropertyBuilder<MapStringIntegerProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP_STRING_INTEGER(final DatastoreEntity owner) {
        super(new MapStringIntegerProperty(owner), null);
    }
}
