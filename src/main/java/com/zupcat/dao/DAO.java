package com.zupcat.dao;

import com.google.appengine.api.datastore.*;
import com.zupcat.cache.MemCache;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.EntityPersistentObjectConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class is a wrapper for Datastore operations. Supports most of the DatastoreService and DatastoreAsyncService operations adding features such as:
 * <p/>
 * - Entity to "DatastoreEntity" convertions
 * - caching usage
 * - retrying algorithms
 * - performance logging
 * - remote client massive and parallel data access
 * <p/>
 * Every X_DataStoreEntity should have its X_DAO implementation. See tests for examples
 */
public abstract class DAO<P extends DatastoreEntity> implements Serializable, IDAO<P> {

    private static final long serialVersionUID = 471847964351314234L;
    private static final RetryingHandler RETRYING_HANDLER = new RetryingHandler();
    protected static final Logger log = Logger.getLogger(DAO.class.getName());

    protected final P sample;
    private final EntityPersistentObjectConverter<P> entityPersistentObjectConverter;


    protected DAO(final P _sample) {
        sample = _sample;
        entityPersistentObjectConverter = new EntityPersistentObjectConverter<>();
    }


    public void updateOrPersist(final P persistentObject) {
        final Entity entity = prepareForUpdateOrPersist(persistentObject);

        getRetryingHandler().tryDSPut(entity);
    }

    public void updateOrPersistAsync(final P persistentObject) {
        final Entity entity = prepareForUpdateOrPersist(persistentObject);

        getRetryingHandler().tryDSPutAsync(entity);
    }

    private Entity prepareForUpdateOrPersist(final P persistentObject) {
        final String entityName = sample.getEntityName();
        final String cacheKey = entityName + persistentObject.getId();
        final MemCache cache = sample.getCacheStrategy().get();

        persistentObject.setModified();

        final Entity entity = buildEntityFromPersistentObject(persistentObject);

        cache.put(cacheKey, entity);

        return entity;
    }


    protected List<P> findByQuery(final Query query) {
        final List<P> list = new ArrayList<>();
        final Iterator<P> iterator = findByQueryIterable(query);

        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    protected Iterator<P> findByQueryIterable(final Query query) {
        return new Iterator<P>() {

            private QueryResultList<Entity> innerResult = null;
            private Iterator<Entity> innerIterator = null;
            private Cursor cursor = null;

            public boolean hasNext() {
                if (innerIterator != null && innerIterator.hasNext()) {
                    return true;
                }

                if (innerIterator != null && cursor == null) {
                    return false;
                }

                final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

                if (cursor != null) {
                    fetchOptions.startCursor(cursor);
                }

                innerResult = getRetryingHandler().tryExecuteQuery(query, fetchOptions);
                cursor = innerResult.getCursor();
                innerIterator = innerResult.iterator();

                return innerResult.size() != 0;
            }

            public P next() {
                return buildPersistentObjectFromEntity(innerIterator.next());
            }

            public void remove() {
                //nothing to do
            }
        };
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

//    public Iterator<C> getByGroupId(final int groupId, final BuildQuery _buildQuery) {
//        final QueryData queryData = new QueryData(getEntityDescriptor().getKind(), null, 150);
//        queryData.addOp(EntityDescriptor.GROUP_ID.getPropertyName(), Query.FilterOperator.EQUAL, groupId);
//
//        _buildQuery.buildQuery(queryData);
//
//        return this.findByQueryLazy(queryData);
//    }

    protected P findUnique(final Query query) {
        final Entity entity = getRetryingHandler().tryExecuteQueryWithSingleResult(query);

        return entity == null ? null : buildPersistentObjectFromEntity(entity);
    }

    public void massiveUpload(final Collection<P> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // nothing to do with cache

        final List<Entity> entities = new ArrayList<>(list.size());

        for (final P persistenceObject : list) {
            entities.add(buildEntityFromPersistentObject(persistenceObject));
        }
        getRetryingHandler().tryDSPutMultipleAsync(entities);
    }

    public void getForMassiveDownload(final MassiveDownload massiveDownload) {
        final String entityName = sample.getEntityName();
        final Query query = new Query(entityName);

        if (!prepareQueryOverride(query, massiveDownload)) {
            final List<Query.Filter> filterList = new ArrayList<>(2);

            filterList.add(new Query.FilterPredicate(sample.GROUP_ID.getPropertyName(), Query.FilterOperator.EQUAL, massiveDownload.getGroupId()));

            if (!massiveDownload.getOnlyUseGroupId()) {
                filterList.add(new Query.FilterPredicate(sample.LAST_MODIFICATION.getPropertyName(), Query.FilterOperator.GREATER_THAN_OR_EQUAL, massiveDownload.getFromFormattedTime() * 10000000));
            }

            query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filterList));
        }

        final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
        final int pageSize = massiveDownload.getPageSize();
        fetchOptions.prefetchSize(pageSize);
        fetchOptions.chunkSize(pageSize);
        fetchOptions.limit(pageSize);

        if (massiveDownload.hasCursor()) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(massiveDownload.getWebCursor()));
        }

        final QueryResultList<Entity> result = getRetryingHandler().tryExecuteQuery(query, fetchOptions);

        if (result.isEmpty()) {
            massiveDownload.setEmpty();
        } else {
            CollectionUtils.filter(result, getFilterPredicate());

            for (final Entity entity : result) {
                postProcessResultEntityWhenDownloading(entity);
            }
            massiveDownload.setResult(result, result.getCursor(), result.size() < pageSize);
        }
    }

    protected Predicate<Entity> getFilterPredicate() {
        // for Override
        return null;
    }

    protected void postProcessResultEntityWhenDownloading(final Entity entity) {
        // for Override
    }

    protected boolean prepareQueryOverride(final Query query, final MassiveDownload massiveDownload) {
        return false;
    }

    public RetryingHandler getRetryingHandler() {
        return RETRYING_HANDLER;
    }

    public static Key buildKey(final String entityName, final String id) {
        return KeyFactory.createKey(entityName, id);
    }

    public Entity buildEntityFromPersistentObject(final P persistentObject) {
        return entityPersistentObjectConverter.buildEntityFromPersistentObject(persistentObject, this);
    }

    public P buildPersistentObjectFromEntity(final Entity entity) {
        return entityPersistentObjectConverter.buildPersistentObjectFromEntity(entity, this);
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
}
