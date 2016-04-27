package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import org.apache.commons.collections4.Predicate;

import java.io.Serializable;
import java.util.*;

/**
 * This class is a wrapper for Datastore operations. Supports most of the DatastoreService and DatastoreAsyncService operations adding features such as:
 * <p/>
 * - Entity to "RedisEntity" convertions
 * - caching usage
 * - retrying algorithms
 * - performance logging
 * - remote client massive and parallel data access
 * <p/>
 * Every X_RedisEntity should have its X_DAO implementation. See tests for examples
 */
public class DAO<P extends RedisEntity> implements Serializable, IDAO<P> {

    //    protected static final Logger log = Logger.getLogger(DAO.class.getName());
    private static final long serialVersionUID = 471847964351314234L;
    private static final RetryingHandler RETRYING_HANDLER = new RetryingHandler();
    //    protected final P sample;
    protected final Class<? extends P> beanClass;
    protected final P sample;


    public DAO(final Class<? extends P> beanClass) {
        this.beanClass = beanClass;
        this.sample = buildPersistentObjectInstance();
    }

    public void updateOrPersist(final P persistentObject) {
        prepareForUpdateOrPersist(persistentObject);

        getRetryingHandler().tryDSPut(persistentObject);
    }

    private void prepareForUpdateOrPersist(final P persistentObject) {
        persistentObject.setModified();
    }

    public P buildRedisEntityFromStringData(final String persistedData) {
        final P result = buildPersistentObjectInstance();

        result.setId(entity.getKey().getName());

        final Text stringData = (Text) entity.getProperty(DATA_CONTAINER_PROPERTY);

        if (stringData != null) {
//                final String stringData = new String(binaryData.getBytes(), Charset.forName("UTF-8"));
//                result.getDataObject().mergeWith(new DataObject(stringData));

            result.getDataObject().mergeWith(new DataObject(stringData.getValue()));

//                objectHolderSerializer.deserialize(binaryData.getBytes(), result.getDataObject(), true);
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

    public List<P> getAll() {
        return findByQuery(null);
    }

    public P findById(final String id) {
        final String entityName = sample.getEntityName();
        final String cacheKey = entityName + id;
        final MemCache cache = sample.getCacheStrategy().get();

        Entity entity = (Entity) cache.get(cacheKey);

        if (entity == null) {
            final Key key = buildKey(entityName, id);

            entity = RETRYING_HANDLER.tryDSGet(key);

            if (entity != null) {
                cache.put(cacheKey, entity);
            }
        }
        return entity == null ? null : buildPersistentObjectFromEntity(entity);
    }

    public Map<String, P> findUniqueIdMultiple(final Collection<String> ids) {
        final Map<String, P> result = new HashMap<>(ids.size());
        final List<Key> realKeys = new ArrayList<>(ids.size());
        final String entityName = sample.getEntityName();

        for (final String key : ids) {
            realKeys.add(buildKey(entityName, key));
        }

        for (final Map.Entry<Key, Entity> entry : getRetryingHandler().tryDSGetMultiple(realKeys).entrySet()) {
            result.put(entry.getKey().getName(), buildPersistentObjectFromEntity(entry.getValue()));
        }
        return result;
    }

    public void remove(final String id) {
        final String entityName = sample.getEntityName();
        final MemCache cache = sample.getCacheStrategy().get();
        final String cacheKey = entityName + id;

        cache.remove(cacheKey);

        getRetryingHandler().tryDSRemove(buildKey(entityName, id));
    }

    public void remove(final Collection<String> ids) {
        final String entityName = sample.getEntityName();
        final MemCache cache = sample.getCacheStrategy().get();
        final List<Key> keys = new ArrayList<>(ids.size());

        for (final String id : ids) {
            final String cacheKey = entityName + id;
            cache.remove(cacheKey);
            keys.add(buildKey(entityName, id));
        }

        getRetryingHandler().tryDSRemove(keys);
    }

    protected Predicate<RedisEntity> getFilterPredicate() {
        // for Override
        return null;
    }

    public RetryingHandler getRetryingHandler() {
        return RETRYING_HANDLER;
    }

    public String getEntityName() {
        return RedisEntity.getEntityName(beanClass);
    }

    @Override
    public List<P> findByIndexableProperty(final String propertyName, final String id) {
        if (id == null || id.trim().length() == 0) {
            return Collections.emptyList();
        }
        return findByQuery(new Query.FilterPredicate(propertyName, Query.FilterOperator.EQUAL, id));
    }

    @Override
    public P buildPersistentObjectInstanceFromPersistedStringData(final String persistedStringData) {
        final P result = buildPersistentObjectInstance();
        result.getDataObject().mergeWith(new DataObject(persistedStringData));

        return result;
    }

    @Override
    public P buildPersistentObjectInstance() {
        try {
            return (P) beanClass.newInstance();
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems instantiating class [" + beanClass.getName() + "]. Maybe missing empty constructor?: " + _exception.getMessage(), _exception);
        }
    }

    public P getSample() {
        return sample;
    }

    @Override
    public P findUniqueByIndexableProperty(final String propertyName, final String id) {
        if (id == null || id.trim().length() == 0) {
            return Collections.emptyList();
        }
        return findByQuery(new Query.FilterPredicate(propertyName, Query.FilterOperator.EQUAL, id));
    }
}
