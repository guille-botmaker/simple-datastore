package com.zupcat.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.zupcat.dao.DAO;
import com.zupcat.model.config.PropertyMeta;
import com.zupcat.property.IntegerProperty;
import com.zupcat.service.SimpleDatastoreServiceFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Helper that converts Datastore Entities to Java Objects
 */
public final class EntityPersistentObjectConverter<P extends DatastoreEntity> {

    private static final Object LOCK_OBJECT = new Object();
    private static EntityPersistentObjectConverter _instance;

    public static final String DATA_CONTAINER_PROPERTY = "bdata";

    private final AvroSerializer<ObjectHolder> objectHolderSerializer;


    private EntityPersistentObjectConverter() {
        objectHolderSerializer = new AvroSerializer<>();
    }

    public static EntityPersistentObjectConverter instance() {
        if (_instance == null) {
            synchronized (LOCK_OBJECT) {
                if (_instance == null) {
                    _instance = new EntityPersistentObjectConverter();
                }
            }
        }
        return _instance;
    }

    public void convertPersistentObjectToStream(final P persistentObject, final ObjectOutputStream outputStream) {
        try {
            outputStream.writeUTF(persistentObject.getEntityName());
            outputStream.writeUTF(persistentObject.getId());
            objectHolderSerializer.serializeTo(persistentObject.getObjectHolder(), ObjectHolder.class, true, outputStream);

        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems converting PO to outputStream for object [" + persistentObject + "]: " + _ioException.getMessage(), _ioException);
        }
    }

    public P getPersistentObjectFromStream(final ObjectInputStream inputStream) {
        try {
            final String entityName = inputStream.readUTF();
            final String id = inputStream.readUTF();

            final ObjectHolder objectHolder = objectHolderSerializer.deserialize(inputStream, ObjectHolder.class, true);

            final DAO dao = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getDAO(entityName);

            final P result = (P) dao.buildPersistentObjectInstance();
            result.setId(id);

            result.getInternalObjectHolder().mergeWith(objectHolder);

            return result;

        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems converting stream to PO: " + _ioException.getMessage(), _ioException);
        }
    }

    public Entity buildEntityFromPersistentObject(final P persistentObject, final DAO<P> dao) {
        final Entity anEntity = new Entity(dao.getEntityName(), persistentObject.getId());

        final byte[] binaryData = objectHolderSerializer.serialize(persistentObject.getObjectHolder(), ObjectHolder.class, true);

        if (binaryData.length > 1000000) {
            throw new RuntimeException("BinaryData length for object [" + persistentObject + "] is bigger than permitted: " + binaryData.length);
        }

        anEntity.setUnindexedProperty(DATA_CONTAINER_PROPERTY, new Blob(binaryData));

        for (final PropertyMeta propertyMeta : persistentObject.getPropertiesMetadata().values()) {
            if (propertyMeta.isIndexable()) {
                anEntity.setProperty(propertyMeta.getPropertyName(), propertyMeta.get());
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
            final ObjectHolder objectHolder = binaryData == null ? null : objectHolderSerializer.deserialize(binaryData.getBytes(), ObjectHolder.class, true);

            if (objectHolder != null) {
                result.getInternalObjectHolder().mergeWith(objectHolder);
            }

            for (final PropertyMeta propertyMeta : result.getPropertiesMetadata().values()) {
                if (propertyMeta.isIndexable()) {
                    final Serializable propertyValue = (Serializable) entity.getProperty(propertyMeta.getPropertyName());

                    if (propertyValue != null && propertyValue.getClass().getName().equals(Long.class.getName()) && propertyMeta.getClass().getName().equals(IntegerProperty.class.getName())) {
                        final Long longValue = (Long) propertyValue;

                        if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                            throw new RuntimeException("Trying to set long value to IntegerProperty. Value was [" + longValue + "], property was [" + propertyMeta.getPropertyName() + "]");
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
