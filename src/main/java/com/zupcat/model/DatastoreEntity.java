package com.zupcat.model;

import com.zupcat.property.IntegerProperty;
import com.zupcat.property.StringProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class DatastoreEntity extends PersistentObject implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private final Map<String, PropertyMeta> propertiesMetadata = new HashMap<>();

    protected DatastoreEntity(final boolean useSessionCache) {
        super(useSessionCache);
    }

    protected StringProperty propString(final String name, final String initialValue) {
        return propString(name, initialValue, false, false, false);
    }

    protected StringProperty propString(final String name, final String initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final StringProperty p = new StringProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        propertiesMetadata.put(name, p);

        return p;
    }

    protected IntegerProperty propInt(final String name, final Integer initialValue) {
        return propInt(name, initialValue, false, false, false);
    }

    protected IntegerProperty propInt(final String name, final Integer initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final IntegerProperty p = new IntegerProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        propertiesMetadata.put(name, p);

        return p;
    }
}
