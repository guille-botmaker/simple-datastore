package com.zupcat.property;

import com.zupcat.model.ObjectVar;
import com.zupcat.model.PersistentObject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMapStringAnyProperty<K, V> extends StringProperty implements Serializable, Map<K, V> {

    private static final long serialVersionUID = 6181606486836703354L;

    private static final String ENTRIES_SEPARATOR = "/";
    private static final String ENTRIES_SEPARATOR_ALT = "[---]";

    private static final String KEY_VALUE_SEPARATOR = "*";
    private static final String KEY_VALUE_SEPARATOR_ALT = "[+++]";

    // to avoid some extra parsing to String
    private transient Map<K, V> cache;


    public AbstractMapStringAnyProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, null, sentToClient, auditable, false);
    }

    @Override
    protected void setValueImpl(String value, final ObjectVar objectVar) {
        if (value != null && value.length() > 0) {
            value = StringUtils.replace(value, ENTRIES_SEPARATOR, ENTRIES_SEPARATOR_ALT);
            value = StringUtils.replace(value, KEY_VALUE_SEPARATOR, KEY_VALUE_SEPARATOR_ALT);
        }
        objectVar.set(name, value);
    }

    private Map<K, V> getMap() {
        if (cache == null) {
            cache = new HashMap<>();
            String data = get();

            if (data != null && data.length() > 0) {
                data = StringUtils.replace(data, ENTRIES_SEPARATOR_ALT, ENTRIES_SEPARATOR);
                data = StringUtils.replace(data, KEY_VALUE_SEPARATOR_ALT, KEY_VALUE_SEPARATOR);

                for (final String entry : StringUtils.split(data, ENTRIES_SEPARATOR)) {
                    final String[] keyValue = StringUtils.split(entry, KEY_VALUE_SEPARATOR);

                    cache.put(convertKeyFromString(keyValue[0]), convertValueFromString(keyValue[1]));
                }
            }
        }
        return cache;
    }


    protected abstract K convertKeyFromString(final String s);

    protected abstract V convertValueFromString(final String s);


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

    public Set<K> keySet() {
        return getMap().keySet();
    }

    public Collection<V> values() {
        return getMap().values();
    }

    public Set<Entry<K, V>> entrySet() {
        return getMap().entrySet();
    }

    // Modification Operations

    public V put(final K key, final V value) {
        final V result = getMap().put(key, value);

        upgradeMap();

        return result;
    }

    public V remove(final Object key) {
        final V result = getMap().remove(key);

        upgradeMap();

        return result;
    }

    public void putAll(final Map<? extends K, ? extends V> m) {
        getMap().putAll(m);

        upgradeMap();
    }

    public void clear() {
        getMap().clear();

        set(null);
    }

    private void upgradeMap() {
        final Map<K, V> map = getMap();
        final StringBuilder builder = new StringBuilder(100);

        for (final Entry<K, V> entry : map.entrySet()) {
            builder.append(entry.getKey()).append(KEY_VALUE_SEPARATOR).append(entry.getValue()).append(ENTRIES_SEPARATOR);
        }

        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
            set(builder.toString());
        } else {
            set(null);
        }
    }
}
