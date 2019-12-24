package io.botmaker.simpleredis.property;

import io.botmaker.simpleredis.model.RedisEntity;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ListJSONObjectProperty<V extends JSONObject> extends ListPrimitiveObjectProperty<JSONObject> {

    private static final long serialVersionUID = 6181623486836703124L;

    private final Class<V> itemClass;

    public ListJSONObjectProperty(final RedisEntity owner, final Class<V> _itemClass, final boolean _keepUniqueElements) {
        super(owner, _keepUniqueElements);

        if (_itemClass == null) throw new RuntimeException("Invalid class!");

        itemClass = _itemClass;
    }

    public List<V> getItemsReadonly() {
        final List<V> convertedList = get().stream().map(this::convert).collect(Collectors.toList());
        return Collections.unmodifiableList(convertedList);
    }

    public void setItems(final List<V> values) {
        super.set((List<JSONObject>) values);
    }

    public V get(final int index) {
        return convert(super.get(index));
    }

    private V convert(final Object value) {

        final JSONObject convertedItem;

        if (!itemClass.isInstance(value)) {
            try {
                convertedItem = itemClass.newInstance();

                final Field field = JSONObject.class.getDeclaredField("map");
                field.setAccessible(true);
                field.set(convertedItem, field.get(value));
            } catch (final Exception _exception) {
                throw new RuntimeException("Could not instantiate object of class [" + itemClass.getName() + "]. Maybe missing empty constructor?: " + _exception.getMessage(), _exception);
            }
        } else {
            convertedItem = (JSONObject) value;
        }

        return (V) convertedItem;
    }
}
