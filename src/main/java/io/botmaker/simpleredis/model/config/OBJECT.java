package io.botmaker.simpleredis.model.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.ObjectProperty;

import java.util.function.Function;

public final class OBJECT<T> extends AbstractPropertyBuilder<ObjectProperty, T> {

    private static final long serialVersionUID = -2702019041291004750L;

    public OBJECT(final RedisEntity owner, final Class<T> itemClass, final Function<ObjectMapper, TypeReference> typeReference) {
        this(owner, itemClass, typeReference, false);
    }

    public OBJECT(final RedisEntity owner, final Class<T> itemClass, final Function<ObjectMapper, TypeReference> typeReference, final boolean compress) {
        super(new ObjectProperty<>(owner, itemClass, typeReference, compress), null);
    }
}
