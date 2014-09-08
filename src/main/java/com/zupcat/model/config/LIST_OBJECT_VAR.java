package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.property.ListObjectVarProperty;

public final class LIST_OBJECT_VAR<OV extends ObjectVar> extends AbstractPropertyBuilder<ListObjectVarProperty, OV> {

    private static final long serialVersionUID = -2702019046191004750L;


    public LIST_OBJECT_VAR(final DatastoreEntity owner, final Class<OV> _objectClass) {
        super(new ListObjectVarProperty<>(owner, _objectClass));
    }
}
