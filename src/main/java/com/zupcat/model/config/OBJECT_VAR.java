package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ObjectVarProperty;

public final class OBJECT_VAR<OV extends ObjectVar> extends AbstractPropertyBuilder<ObjectVarProperty, OV> {

    private static final long serialVersionUID = -2702019046191004750L;


    public OBJECT_VAR(final DatastoreEntity owner, final Class<OV> _objectClass) {
        super(new ObjectVarProperty<>(owner, _objectClass), null);
    }
}
