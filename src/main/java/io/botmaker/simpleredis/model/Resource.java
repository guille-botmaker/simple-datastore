package io.botmaker.simpleredis.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import io.botmaker.simpleredis.cache.CacheStrategy;
import io.botmaker.simpleredis.dao.DAO;
import io.botmaker.simpleredis.dao.RetryingHandler;
import io.botmaker.simpleredis.dao.SerializationHelper;

import java.io.Serializable;

/**
 * Usefull class for app parameters persistence.
 */
public final class Resource implements Serializable {

    public static final int MAX_BLOB_SIZE = 1150 * 1024; //(1150k);
    private static final long serialVersionUID = -2259862423311176614L;
    private static final String ENTITY_NAME = "Resource";
    private String id;
    private String type;
    private byte[] rawValue;


    public Resource() {
        // nothing to do
    }

    public Resource(final Resource other) {
        this.id = other.id;
        this.type = other.type;
        this.rawValue = other.rawValue;
    }

    public static Resource load(final String id) {
        return load(id, true);
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

    public static Resource buildJavaObject(final String id, final Object javaObject) {
        final Resource resource = new Resource();

        resource.id = id;
        resource.type = "application/x-java-serialized-object";
        resource.rawValue = javaObject == null ? null : SerializationHelper.getBytes(javaObject);

        return resource;
    }

    public String getType() {
        return type;
    }

    public Object getJavaObject() {
        return rawValue == null ? null : SerializationHelper.getObjectFromBytes(rawValue);
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
            throw new RuntimeException("Resource with key [" + id + "] is bigger than permitted(" + MAX_BLOB_SIZE + "): " + this.rawValue.length);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Resource resource = (Resource) o;

        return !(id != null ? !id.equals(resource.id) : resource.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
