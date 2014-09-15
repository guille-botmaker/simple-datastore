package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.MapStringMapStringStringProperty;

public final class MAP_STRING_MAP_STRING_STRING extends AbstractPropertyBuilder<MapStringMapStringStringProperty, String> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP_STRING_MAP_STRING_STRING(final DatastoreEntity owner) {
        super(new MapStringMapStringStringProperty(owner), null);
    }
}
