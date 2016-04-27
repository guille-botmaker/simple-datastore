package io.botmaker.simpleredis.dao;

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
    protected final P sample;


    public DAO(final Class<? extends P> _beanClass) {
        try {
            sample = _beanClass.newInstance();
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems instantiating class [" + _beanClass.getName() + "] (maybe a missing default empty constructor?): " + _exception.getMessage(), _exception);
        }
    }

    public void updateOrPersist(final P persistentObject) {
        prepareForUpdateOrPersist(persistentObject);

        getRetryingHandler().tryDSPut(persistentObject);
    }

    public void updateOrPersistAsync(final P persistentObject) {
        prepareForUpdateOrPersist(persistentObject);

        getRetryingHandler().tryDSPutAsync(persistentObject);
    }

    private void prepareForUpdateOrPersist(final P persistentObject) {
        persistentObject.setModified();
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

    public FutureEntity<P> findByIdAsync(final String id) {
        final String entityName = sample.getEntityName();
        final String cacheKey = entityName + id;
        final MemCache cache = sample.getCacheStrategy().get();

        final Entity cachedEntity = (Entity) cache.get(cacheKey);

        if (cachedEntity == null) {
            final Key key = buildKey(entityName, id);

            return new FutureEntity<>(key, this, cache, cacheKey);
        } else {
            return new FutureEntity<>(buildPersistentObjectFromEntity(cachedEntity));
        }
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

    public void removeAsync(final String id) {
        final String entityName = sample.getEntityName();
        final MemCache cache = sample.getCacheStrategy().get();
        final String cacheKey = entityName + id;

        cache.remove(cacheKey);

        getRetryingHandler().tryDSRemoveAsync(buildKey(entityName, id));
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

    protected void postProcessResultEntityWhenDownloading(final RedisEntity entity) {
        // for Override
    }

    public RetryingHandler getRetryingHandler() {
        return RETRYING_HANDLER;
    }

    public String getEntityName() {
        return sample.getEntityName();
    }

    public P buildPersistentObjectInstance() {
        try {
            return (P) sample.getClass().newInstance();
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems instantiating class [" + sample.getClass().getName() + "]: " + _exception.getMessage(), _exception);
        }
    }

    public List<P> findByIndexableProperty(final String propertyName, final String id) {
        if (id == null || id.trim().length() == 0) {
            return Collections.emptyList();
        }
        return findByQuery(new Query.FilterPredicate(propertyName, Query.FilterOperator.EQUAL, id));
    }

    public P getReadonlySample() {
        return sample;
    }
}
