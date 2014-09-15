package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.WithIdObjectVar;
import com.zupcat.property.MapStringObjectVarProperty;

import java.util.Map;

public final class MAP_STRING_OBJECT_VAR<OV extends WithIdObjectVar> extends AbstractPropertyBuilder<MapStringObjectVarProperty, Map<String, ? extends OV>> {

    private static final long serialVersionUID = -2702019046191004750L;


    public MAP_STRING_OBJECT_VAR(final DatastoreEntity owner, final Class<OV> _objectClass) {
        super(new MapStringObjectVarProperty<>(owner, _objectClass), null);
    }
}
