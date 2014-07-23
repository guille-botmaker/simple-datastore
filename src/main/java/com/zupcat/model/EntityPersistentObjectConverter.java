package com.zupcat.model;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.zupcat.dao.DAO;

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

            result.setIdForDeserializationProcess(entity.getKey().getName());

            final Blob binaryData = (Blob) entity.getProperty(DATA_CONTAINER_PROPERTY);
            final ObjectHolder objectHolder = binaryData == null ? null : ObjectHolder.deserialize(binaryData.getBytes(), true);

            if (objectHolder != null) {
                result.getObjectHolder().mergeWith(objectHolder);
            }
        }
        return result;
    }
}
