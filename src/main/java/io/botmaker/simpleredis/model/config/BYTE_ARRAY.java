package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.ByteArrayProperty;

public final class BYTE_ARRAY extends AbstractPropertyBuilder<ByteArrayProperty, byte[]> {

    private static final long serialVersionUID = -2702019046191004750L;

    public BYTE_ARRAY(final RedisEntity owner) {
        super(new ByteArrayProperty(owner), null);
    }
}