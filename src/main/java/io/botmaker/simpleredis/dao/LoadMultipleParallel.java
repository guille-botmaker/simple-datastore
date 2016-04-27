package io.botmaker.simpleredis.dao;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.Query;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import io.botmaker.simpleredis.util.CommonsParallel;

import java.util.*;

public final class LoadMultipleParallel<E extends RedisEntity> extends CommonsParallel<List<String>> {

    private static final int QUERY_PACK_SIZE = 100;

    private final Object LOCK_OBJECT = new Object();
    private final List<String> ids;
    private final Map<String, E> result;
    private final DAO<E> dao;
    private final PropertyMeta propertyMeta;


    public LoadMultipleParallel(final Collection<String> _ids, final DAO<E> _dao) {
        this(_ids, _dao, null);
    }

    public LoadMultipleParallel(final Collection<String> _ids, final DAO<E> _dao, final PropertyMeta _propertyMeta) {
        super((_ids.size() / QUERY_PACK_SIZE) < 1 ? 1 : (_ids.size() / QUERY_PACK_SIZE), ThreadManager.currentRequestThreadFactory());

        ids = new ArrayList<>(_ids);
        result = new HashMap<>(_ids.size());
        dao = _dao;
        propertyMeta = _propertyMeta;
    }


    @Override
    protected void preExecution() {
        final Iterator<String> iterator = ids.iterator();
        final List<String> idPack = new ArrayList<>(QUERY_PACK_SIZE);

        while (iterator.hasNext()) {
            idPack.add(iterator.next());

            if (idPack.size() >= QUERY_PACK_SIZE) {
                workOnThread(new ArrayList<>(idPack));
                idPack.clear();
            }
        }

        if (!idPack.isEmpty()) {
            workOnThread(new ArrayList<>(idPack));
        }
    }

    public Map<String, E> getResult() {
        return result;
    }

    @Override
    protected void doWorkConcurrently(final List<String> ids) throws Exception {
        Map<String, E> tempResult = new HashMap<>(QUERY_PACK_SIZE);

        if (propertyMeta == null) {
            tempResult.putAll(dao.findUniqueIdMultiple(ids));

        } else {
            final Query.FilterPredicate filterPredicate = new Query.FilterPredicate(propertyMeta.getPropertyName(), Query.FilterOperator.IN, ids);

            for (final E item : dao.findByQuery(filterPredicate)) {
                tempResult.put(item.getId(), item);
            }
        }

        if (!tempResult.isEmpty()) {
            synchronized (LOCK_OBJECT) {
                result.putAll(tempResult);
            }
        }
    }
}
