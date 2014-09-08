package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapStringMapStringIntegerProperty;

public final class MAP_STRING_MAP_STRING_INTEGER extends AbstractPropertyBuilder<MapStringMapStringIntegerProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP_STRING_MAP_STRING_INTEGER(final DatastoreEntity owner) {
        super(new MapStringMapStringIntegerProperty(owner));
    }
}
