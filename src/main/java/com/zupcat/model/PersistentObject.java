package com.zupcat.model;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.util.RandomUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class PersistentObject implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public static final String DATE_FORMAT = "yyMMddHHmmssSSS";

    private String id;
    private final ObjectHolder objectHolder = new ObjectHolder();
    private final Map<String, PropertyMeta> propertiesMetadata = new HashMap<>();
    private final String entityName;
    private final CacheStrategy cacheStrategy;


    protected PersistentObject(final CacheStrategy cacheStrategy) {
        final String className = this.getClass().getName();

        this.cacheStrategy = cacheStrategy;
        entityName = className.substring(className.lastIndexOf("."));
        id = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        setModified();
    }

    public abstract void setModified();

    public String getId() {
        return id;
    }

    protected void setIdForDeserializationProcess(final String id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public Map<String, PropertyMeta> getPropertiesMetadata() {
        return propertiesMetadata;
    }

    protected void addPropertyMeta(final String name, final PropertyMeta propertyMeta) {
        if (propertyMeta.isIndexable() && propertyMeta.getInitialValue() == null) {
            throw new RuntimeException("Property [" + name + "] of Entity [" + entityName + "] is indexable and has null default value. This is not allowed. Please change then initialValue to be not null");
        }
        propertiesMetadata.put(name, propertyMeta);
    }

    /**
     * For framework internal calls. Do not use this method directly
     */
    public ObjectHolder getObjectHolder() {
        return objectHolder;
    }

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(500);

        builder
                .append("[")
                .append(entityName)
                .append("|")
                .append(id)
                .append("|")
                .append(objectHolder.toString(builder));

        builder.append("]");

        return builder.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PersistentObject that = (PersistentObject) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
