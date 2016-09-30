package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.model.RedisEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class is a wrapper for Datastore operations. Supports most of the DatastoreService and DatastoreAsyncService operations adding features such as:
 * <p>
 * - Entity to "DatastoreEntity" convertions
 * - caching usage
 * - retrying algorithms
 * - performance logging
 * - remote client massive and parallel data access
 * <p>
 * Every X_DataStoreEntity should have its X_DAO implementation. See tests for examples
 */
public interface IDAO<P extends RedisEntity> extends Serializable {

    P buildPersistentObjectInstance();

    P buildPersistentObjectInstanceFromPersistedStringData(final String persistedStringData);

    // Querying methods =====

    P findById(final String id);

    Map<String, P> findUniqueIdMultiple(final Collection<String> ids);

    List<P> getAll();

    P findUniqueByIndexableProperty(final String propertyName, final String id);

    List<P> findMultipleByIndexableProperty(final String propertyName, final String id);

    List<P> findMultipleIntersectionOfIndexableProperty(final Map<String, String> propertyNameAndValueMap);

    List<P> findMultipleLastOccurrencesByIndexableProperty(final String propertyName, final int ocurrences, final String id);

    List<P> findMultipleUnionOfIndexableProperty(final List<Pair<String, String>> propertyNameAndValuePair);

    List<P> findMultipleSortedFromToByIndexableProperty(final String propertyName, final String id, final int from, final int to);

    void save(final P persistentObject);

    void massiveUpload(final Collection<P> list);

    void remove(final String id);

    void remove(final Collection<String> ids);
}
