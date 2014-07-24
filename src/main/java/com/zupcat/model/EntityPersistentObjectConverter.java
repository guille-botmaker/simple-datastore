package com.zupcat.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.zupcat.dao.DAO;
import com.zupcat.property.IntegerProperty;

import java.io.Serializable;

public final class EntityPersistentObjectConverter<P extends DatastoreEntity> {

    public static final String DATA_CONTAINER_PROPERTY = "bdata";

    public Entity buildEntityFromPersistentObject(final P persistentObject, final DAO<P> dao) {
        final Entity anEntity = new Entity(dao.getEntityName(), persistentObject.getId());

        final byte[] binaryData = persistentObject.getObjectHolder().serialize(true);

        if (binaryData.length > 1000000) {
            throw new RuntimeException("BinaryData length for object [" + persistentObject + "] is bigger than permitted: " + binaryData.length);
        }

        anEntity.setUnindexedProperty(DATA_CONTAINER_PROPERTY, new Blob(binaryData));

        for (final PropertyMeta propertyMeta : persistentObject.getPropertiesMetadata().values()) {
            if (propertyMeta.isIndexable()) {
                anEntity.setProperty(propertyMeta.getName(), propertyMeta.get());
            }
        }
        return anEntity;
    }

    public P buildPersistentObjectFromEntity(final Entity entity, final DAO<P> dao) {
        P result = null;

        if (entity != null) {
            result = dao.buildPersistentObjectInstance();

            result.setId(entity.getKey().getName());

            final Blob binaryData = (Blob) entity.getProperty(DATA_CONTAINER_PROPERTY);
            final ObjectHolder objectHolder = binaryData == null ? null : ObjectHolder.deserialize(binaryData.getBytes(), true);

            if (objectHolder != null) {
                result.getObjectHolder().mergeWith(objectHolder);
            }

            for (final PropertyMeta propertyMeta : result.getPropertiesMetadata().values()) {
                if (propertyMeta.isIndexable()) {
                    final Serializable propertyValue = (Serializable) entity.getProperty(propertyMeta.getName());

                    if (propertyValue != null && propertyValue.getClass().getName().equals(Long.class.getName()) && propertyMeta.getClass().getName().equals(IntegerProperty.class.getName())) {
                        final Long longValue = (long) propertyValue;

                        if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                            throw new RuntimeException("Trying to set long value to IntegerProperty. Value was [" + longValue + "], property was [" + propertyMeta.getName() + "]");
                        }

                        propertyMeta.set(longValue.intValue());
                    } else {
                        propertyMeta.set(propertyValue);
                    }
                }
            }
        }
        return result;
    }
}
