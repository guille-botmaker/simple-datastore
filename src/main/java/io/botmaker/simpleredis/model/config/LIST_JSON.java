package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.property.ListJSONObjectProperty;
import org.json.JSONObject;

public final class LIST_JSON<T extends JSONObject> extends AbstractPropertyBuilder<ListJSONObjectProperty<T>, T> {

    private static final long serialVersionUID = -2604557890091004750L;

    public LIST_JSON(final RedisEntity owner, final Class<T> itemClass) {
        this(owner, itemClass, false);
    }

    public LIST_JSON(final RedisEntity owner, final Class<T> itemClass, final boolean keepUniqueElements) {
        super(new ListJSONObjectProperty<T>(owner, itemClass, keepUniqueElements), null);
    }
}
