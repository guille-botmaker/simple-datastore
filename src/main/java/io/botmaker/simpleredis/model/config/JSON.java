package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.JSONProperty;

import java.util.HashMap;

public final class JSON extends AbstractPropertyBuilder<JSONProperty, HashMap<String, Object>> {

    private static final long serialVersionUID = -2702019041291004750L;

    public JSON(final RedisEntity owner) {
        super(new JSONProperty(owner), new HashMap<>());
    }
}
