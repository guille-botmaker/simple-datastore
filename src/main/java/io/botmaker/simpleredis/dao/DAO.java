package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import org.apache.commons.collections4.Predicate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    public List<P> getAll() {
        return getRetryingHandler().tryDSGetAll(this);
    }

    public P findById(final String id) {
        return RETRYING_HANDLER.tryDSGet(id, this);
    }

    public Map<String, P> findUniqueIdMultiple(final Collection<String> ids) {
        return getRetryingHandler().tryDSGetMultiple(ids, this);
    }

    public void remove(final String id) {
        getRetryingHandler().tryDSRemove(id, this);
    }

    public void remove(final Collection<String> ids) {
        getRetryingHandler().tryDSRemove(ids, this);
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
    public P buildPersistentObjectInstanceFromPersistedStringData(final String persistedStringData) {
        final P result = buildPersistentObjectInstance();
        result.getDataObject().mergeWith(new DataObject(persistedStringData));

        return result;
    }

    @Override
    public P buildPersistentObjectInstance() {
        try {
            return beanClass.newInstance();
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
            return null;
        }
        return getRetryingHandler().tryDSGetByIndexableProperty(propertyName, id, this);
    }
}
