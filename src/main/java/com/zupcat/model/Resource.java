package com.zupcat.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.zupcat.cache.CacheStrategy;
import com.zupcat.dao.DAO;
import com.zupcat.dao.RetryingHandler;
import com.zupcat.dao.SerializationHelper;

import java.io.Serializable;

public final class Resource<T> implements Serializable {

    private static final long serialVersionUID = -2259862423311176614L;
    private static final String ENTITY_NAME = "Resource";

    public static final int MAX_BLOB_SIZE = 1100 * 1024; //(1100k);

    private String id;
    private String type;
    private byte[] rawValue;


    public String getType() {
        return type;
    }

    public T getJavaObject() {
        return rawValue == null ? null : (T) SerializationHelper.getObjectFromCompressedBytes(rawValue);
    }

    public byte[] getRawValue() {
        return rawValue;
    }

    public void save() {
        save(false);
    }

    public void save(final boolean saveAsync) {
        validateSize();

        final Entity anEntity = new Entity(ENTITY_NAME, id);

        if (type != null) {
            anEntity.setProperty("type", type);
        }
        if (rawValue != null) {
            anEntity.setUnindexedProperty("bdata", new Blob(rawValue));
        }

        final RetryingHandler retryingHandler = new RetryingHandler();

        if (saveAsync) {
            retryingHandler.tryDSPutAsync(anEntity);
        } else {
            retryingHandler.tryDSPut(anEntity);
        }
        CacheStrategy.APPLICATION_CACHE.get().put(ENTITY_NAME + id, this);
    }

    public void validateSize() {
        if (this.rawValue != null && this.rawValue.length > MAX_BLOB_SIZE) {
            throw new RuntimeException("Resource with key [" + id + "] is bigger than permitted: " + this.rawValue.length);
        }
    }

    public static Resource load(final String key) {
        return load(key, true);
    }

    public static Resource load(final String id, final boolean exceptionOnNotFound) {
        Resource result = (Resource) CacheStrategy.APPLICATION_CACHE.get().get(ENTITY_NAME + id);

        if (result == null) {
            final Entity entity = new RetryingHandler().tryDSGet(DAO.buildKey(ENTITY_NAME, id));

            if (entity != null) {
                result = new Resource();
                result.id = id;
                result.type = (String) entity.getProperty("type");

                final Blob binaryData = (Blob) entity.getProperty("bdata");
                result.rawValue = binaryData == null ? null : binaryData.getBytes();
            }
        }

        if (result == null && exceptionOnNotFound) {
            throw new RuntimeException("Could not find resource with key [" + id + "]");
        }
        return result;
    }

    public static Resource buildUnknownType(final String id, final String type, final byte[] content) {
        final Resource resource = new Resource();

        resource.id = id;
        resource.type = type;
        resource.rawValue = content;

        return resource;
    }

    public static Resource buildJavaObject(final String id, final Serializable javaObject) {
        final Resource resource = new Resource();

        resource.id = id;
        resource.type = "application/x-java-serialized-object";
        resource.rawValue = javaObject == null ? null : SerializationHelper.getCompressedBytes(javaObject);

        return resource;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Resource resource = (Resource) o;

        if (id != null ? !id.equals(resource.id) : resource.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
