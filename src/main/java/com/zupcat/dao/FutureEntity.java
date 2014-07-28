package com.zupcat.dao;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.zupcat.cache.MemCache;
import com.zupcat.model.PersistentObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper for async Datastore ops
 */
public final class FutureEntity<T extends PersistentObject> {

    private final Future<Entity> futureEntity;
    private final Future<T> future;
    private final MemCache cache;
    private final String cacheKey;
    private final DAO dao;

    public FutureEntity(final T _result) {
        futureEntity = null;
        cache = null;
        cacheKey = null;
        dao = null;

        future = new Future<T>() {

            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                return _result;
            }

            @Override
            public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return _result;
            }
        };

    }

    public FutureEntity(final Key key, final DAO dao, final MemCache cache, final String cacheKey) {
        this.cache = cache;
        this.cacheKey = cacheKey;
        this.futureEntity = dao.getRetryingHandler().tryDSGetAsync(key);
        this.dao = dao;

        this.future = null;
    }

    public T get() {
        try {
            return getImpl();
        } catch (final Exception _exception) {
            throw new RuntimeException(_exception);
        }
    }

    private T getImpl() throws ExecutionException, InterruptedException {
        if (future != null) {
            return future.get();
        } else {
            final Entity entity = futureEntity.get();

            if (entity != null) {
                cache.put(cacheKey, entity);

                return (T) dao.buildPersistentObjectFromEntity(entity);
            }
            return null;
        }
    }
}
