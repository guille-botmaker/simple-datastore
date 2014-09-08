package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.ByteArrayProperty;

public final class BYTE_ARRAY extends AbstractPropertyBuilder<ByteArrayProperty, byte[]> {

    private static final long serialVersionUID = -2702019046191004750L;


    public BYTE_ARRAY(final DatastoreEntity owner) {
        super(new ByteArrayProperty(owner));
    }
}
