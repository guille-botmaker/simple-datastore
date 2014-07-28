package com.zupcat.property;

import com.zupcat.model.ObjectVar;
import com.zupcat.model.PersistentObject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapStringStringProperty extends StringProperty implements Serializable, Map<String, String> {

    private static final long serialVersionUID = 6181606486836703354L;
    private static final String ENTRIES_SEPARATOR = "/";
    private static final String ENTRIES_SEPARATOR_ALT = "[---]";

    private static final String KEY_VALUE_SEPARATOR = "*";
    private static final String KEY_VALUE_SEPARATOR_ALT = "[+++]";

    // to avoid some extra parsing to String
    private final transient Map<String, String> cache = new HashMap<>();


    public MapStringStringProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
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

    private Map<String, String> getMap() {
        if (cache.isEmpty()) {
            String data = get();

            if (data != null && data.length() > 0) {
                data = StringUtils.replace(data, ENTRIES_SEPARATOR_ALT, ENTRIES_SEPARATOR);
                data = StringUtils.replace(data, KEY_VALUE_SEPARATOR_ALT, KEY_VALUE_SEPARATOR);

                for (final String entry : StringUtils.split(data, ENTRIES_SEPARATOR)) {
                    final String[] keyValue = StringUtils.split(entry, KEY_VALUE_SEPARATOR);

                    cache.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return cache;
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

    public String get(final Object key) {
        return getMap().get(key);
    }

    public Set<String> keySet() {
        return getMap().keySet();
    }

    public Collection<String> values() {
        return getMap().values();
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return getMap().entrySet();
    }

    // Modification Operations

    public String put(final String key, final String value) {
        final String result = getMap().put(key, value);

        upgradeMap();

        return result;
    }

    public String remove(final Object key) {
        final String result = getMap().remove(key);

        upgradeMap();

        return result;
    }

    public void putAll(final Map<? extends String, ? extends String> m) {
        getMap().putAll(m);

        upgradeMap();
    }

    public void clear() {
        getMap().clear();

        set(null);
    }

    private void upgradeMap() {
        final Map<String, String> map = getMap();
        final StringBuilder builder = new StringBuilder(100);

        for (final Entry<String, String> entry : map.entrySet()) {
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
