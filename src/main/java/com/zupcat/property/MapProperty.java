package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Work as Map<String, Object>
 */
public final class MapProperty<V> extends PropertyMeta<Map<String, V>> implements Serializable, Map<String, V> {

    private static final long serialVersionUID = 6181606486836703354L;


    public MapProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected Map<String, V> getValueImpl(final DataObject dataObject) {
        return getMapImpl(dataObject);
    }

    @Override
    protected void setValueImpl(final Map<String, V> value, final DataObject dataObject) {
        if (value == null || value.isEmpty()) {
            dataObject.remove(name);
        } else {
            dataObject.put(name, new DataObject(value));
        }
    }


    private Map<String, V> getMap() {
        return getMapImpl(getOwner().getDataObject());
    }

    private Map<String, V> getMapImpl(final DataObject dataObject) {
        final JSONObject jsonObject;

        if (dataObject.has(name)) {
            jsonObject = dataObject.getJSONObject(name);
        } else {
            jsonObject = new JSONObject();
            dataObject.put(name, jsonObject);
        }
        return getInternalMapFromJSONObject(jsonObject);
    }


    private Map<String, V> getInternalMapFromJSONObject(final JSONObject jsonObject) {
        try {
            final Field arrayField = jsonObject.getClass().getDeclaredField("map");

            arrayField.setAccessible(true);

            return (Map<String, V>) arrayField.get(jsonObject);
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems when getting JSONObject internal map field using reflection for array [" + jsonObject + ": " + _exception.getMessage(), _exception);
        }
    }


    // Reading operations
    public int size() {
        return getMap().size();
    }

    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    public boolean containsKey(final Object key) {
        return getMap().containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return getMap().containsValue(value);
    }

    public V get(final Object key) {
        return getMap().get(key);
    }

    public Set<String> keySet() {
        return getMap().keySet();
    }

    public Collection<V> values() {
        return getMap().values();
    }

    public Set<Entry<String, V>> entrySet() {
        return getMap().entrySet();
    }


    // Modification Operations
    public V put(final String key, final V value) {
        return getMap().put(key, value);
    }

    public V remove(final Object key) {
        return getMap().remove(key);
    }

    public void putAll(final Map<? extends String, ? extends V> m) {
        getMap().putAll(m);
    }

    public void clear() {
        getMap().clear();
    }
}
