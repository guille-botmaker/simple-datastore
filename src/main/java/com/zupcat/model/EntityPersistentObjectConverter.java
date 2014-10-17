package com.zupcat.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.zupcat.dao.DAO;
import com.zupcat.model.config.PropertyMeta;
import com.zupcat.property.IntegerProperty;
import com.zupcat.service.SimpleDatastoreServiceFactory;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Helper that converts Datastore Entities to Java Objects
 */
public final class EntityPersistentObjectConverter<P extends DatastoreEntity> {

    private static final Object LOCK_OBJECT = new Object();
    private static EntityPersistentObjectConverter _instance;

    public static final String DATA_CONTAINER_PROPERTY = "bdata";

    private final DataObjectSerializer<DataObject> objectHolderSerializer;


    private EntityPersistentObjectConverter() {
        objectHolderSerializer = new DataObjectSerializer<>();
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
            outputStream.write(objectHolderSerializer.serialize(persistentObject.getDataObject(), true));

        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems converting PO to outputStream for object [" + persistentObject + "]: " + _ioException.getMessage(), _ioException);
        }
    }

    public P getPersistentObjectFromStream(final ObjectInputStream inputStream) {
        try {
            final String entityName = inputStream.readUTF();
            final String id = inputStream.readUTF();

            final DAO dao = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getDAO(entityName);
            final P result = (P) dao.buildPersistentObjectInstance();
            result.setId(id);

            objectHolderSerializer.deserialize(IOUtils.toByteArray(inputStream), result.getDataObject(), true);

            return result;

        } catch (final EOFException _eofException) {
            return null;

        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems converting stream to PO: " + _ioException.getMessage(), _ioException);
        }
    }

//    public Map<String, Object> buildMapFromPersistentObject(final P persistentObject) {
//        final Map<String, Object> result = new HashMap<>();
//
//        if (persistentObject != null) {
//            result.put("___id___", persistentObject.getId());
//            result.put("___entityName___", persistentObject.getEntityName());
//
//            final byte[] binaryData = objectHolderSerializer.serialize(persistentObject.getDataObject(), true);
//
//            result.put(DATA_CONTAINER_PROPERTY, binaryData);
//
//            for (final PropertyMeta propertyMeta : persistentObject.getPropertiesMetadata().values()) {
//                if (propertyMeta.isIndexable()) {
//                    result.put(propertyMeta.getPropertyName(), propertyMeta.get());
//                }
//            }
//        }
//        return result;
//    }

//    public P buildPersistentObjectFromMap(final Map<String, Object> map, final DAO<P> dao) {
//        P result = null;
//
//        if (map != null && !map.isEmpty()) {
//            result = dao.buildPersistentObjectInstance();
//
//            result.setId(map.get("___id___").toString());
//
//            final byte[] binaryData = (byte[]) map.get(DATA_CONTAINER_PROPERTY);
//
//            if (binaryData != null) {
//                objectHolderSerializer.deserialize(binaryData, result.getDataObject(), true);
//            }
//
//            for (final PropertyMeta propertyMeta : result.getPropertiesMetadata().values()) {
//                if (propertyMeta.isIndexable()) {
//                    final Serializable propertyValue = (Serializable) map.get(propertyMeta.getPropertyName());
//
//                    if (propertyValue != null && propertyValue.getClass().getName().equals(Long.class.getName()) && propertyMeta.getClass().getName().equals(IntegerProperty.class.getName())) {
//                        final Long longValue = (Long) propertyValue;
//
//                        if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
//                            throw new RuntimeException("Trying to set long value to IntegerProperty. Value was [" + longValue + "], property was [" + propertyMeta.getPropertyName() + "]");
//                        }
//
//                        propertyMeta.set(longValue.intValue());
//                    } else {
//                        propertyMeta.set(propertyValue);
//                    }
//                }
//            }
//        }
//        return result;
//    }

    public Entity buildEntityFromPersistentObject(final P persistentObject, final DAO<P> dao) {
        if (persistentObject == null) {
            return null;
        }

        final Entity anEntity = new Entity(dao.getEntityName(), persistentObject.getId());

        final byte[] binaryData = objectHolderSerializer.serialize(persistentObject.getDataObject(), true);

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

            if (binaryData != null) {
                objectHolderSerializer.deserialize(binaryData.getBytes(), result.getDataObject(), true);
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
