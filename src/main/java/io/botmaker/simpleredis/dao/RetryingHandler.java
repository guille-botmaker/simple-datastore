package io.botmaker.simpleredis.dao;

import com.google.appengine.api.datastore.*;
import io.botmaker.simpleredis.exception.NoMoreRetriesException;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.service.RedisServer;
import io.botmaker.simpleredis.service.SimpleDatastoreService;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proxy for all Datastore ops. It retries the call if it has problems
 *
 * @see "http://code.google.com/appengine/articles/handling_datastore_errors.html"
 * @see "http://code.google.com/appengine/docs/java/datastore/transactions.html"
 */
public final class RetryingHandler implements Serializable {

    private static final long serialVersionUID = 472842924253314234L;
    private static final Logger LOGGER = Logger.getLogger(RetryingHandler.class.getName());

    private static final int MAX_RETRIES = 3;
    private static final int WAIT_MS = 800;

    public static void sleep(final int millis) {
        try {
            Thread.sleep(millis);

        } catch (final InterruptedException ie) {
            // nothing to do
        }
    }

    public void tryDSRemove(final Collection<String> entityKeys) {
        tryClosure((datastore, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSRemoveMultiple", new Exception());
            }

            datastore.delete(entityKeys);
        }, null);
    }

    public RedisEntity tryDSGet(final String entityKey) {
        final RedisEntity[] result = new RedisEntity[1];

        tryClosure((datastore, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGet", new Exception());
            }

            Entity resultEntity = null;

            try {
                resultEntity = datastore.get(entityKey);
            } catch (final EntityNotFoundException _entityNotFoundException) {
                // nothing to do
            }
            results[0] = resultEntity;
        }, result);

        return result[0];
    }

    public Future<Entity> tryDSGetAsync(final Key entityKey) {
        return tryClosureAsync((datastore, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetAsync", new Exception());
            }

            return datastore.get(entityKey);
        });
    }

    public void tryDSRemove(final Key entityKey) {
        tryClosure((datastore, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSRemove", new Exception());
            }

            datastore.delete(entityKey);
        }, null);
    }

    public void tryDSRemoveAsync(final Key key) {
        tryClosureAsync((datastore, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSRemoveAsync", new Exception());
            }

            return datastore.delete(key);
        });
    }

    public void tryDSPutMultipleAsync(final Iterable<Entity> entities) {
        tryClosureAsync((datastore, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSPutMultipleAsync", new Exception());
            }

            final Future<List<Key>> listFuture = datastore.put(entities);

            listFuture.get();

            return listFuture;
        });
    }

    public Map<Key, Entity> tryDSGetMultiple(final Collection<Key> keys) {
        final Map<Key, Entity> result = new HashMap<>();

        if (keys != null && !keys.isEmpty()) {
            tryClosure((datastore, results, loggingActivated) -> {
                if (loggingActivated) {
                    LOGGER.log(Level.SEVERE, "PERF - tryDSGetMultiple", new Exception());
                }

                result.putAll(datastore.get(keys));
            }, null);
        }
        return result;
    }

    public void tryDSPut(final RedisEntity entity) {
        tryClosure((redisServer, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSPut", new Exception());
            }

            try (redisServer.getPool().getResource()) {

            }


            datastore.put(entity);
        }, null);
    }

    public void tryDSPutAsync(final Entity entity) {
        tryClosureAsync((datastore, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSPutAsync", new Exception());
            }

            return datastore.put(entity);
        });
    }

    private void tryClosure(final Closure closure, final Object[] results) {
        final ValuesContainer values = new ValuesContainer();
        final SimpleDatastoreService simpleDatastoreService = SimpleDatastoreServiceFactory.getSimpleDatastoreService();
        final RedisServer redisServer = simpleDatastoreService.getRedisServer();
        final boolean loggingActivated = simpleDatastoreService.isDatastoreCallsLoggingActivated();

        while (true) {
            try {
                closure.execute(datastore, results, loggingActivated);
                break;
            } catch (final Exception exception) {
                handleError(values, exception, false);
            }
        }
    }

    private <T> Future<T> tryClosureAsync(final AsyncClosure<T> closure) {
        final ValuesContainer values = new ValuesContainer();
        final AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
        Future<T> result;
        final SimpleDatastoreService simpleDatastoreService = SimpleDatastoreServiceFactory.getSimpleDatastoreService();

        final boolean loggingActivated = simpleDatastoreService.isDatastoreCallsLoggingActivated();

        while (true) {
            try {
                result = closure.execute(datastore, loggingActivated);
                break;
            } catch (final Exception exception) {
                handleError(values, exception, false);
            }
        }
        return result;
    }

    private void handleError(final ValuesContainer values, final Exception exception, final boolean isTimeoutException) {
        values.retry = values.retry - 1;

        if (values.retry == 0) {
            LOGGER.log(Level.SEVERE, "PERF - No more tries for datastore access: " + exception.getMessage(), exception);
            throw new NoMoreRetriesException(exception);
        }

        sleep(values.retryWait);

        if (isTimeoutException) {
            values.retryWait = values.retryWait * 3;
        }
    }

    private interface Closure {

        void execute(final RedisServer redisServer, final Object[] results, final boolean loggingActivated);
    }

    private interface AsyncClosure<T> {

        Future<T> execute(final RedisServer redisServer, final boolean loggingActivated) throws Exception;
    }

    public static final class ValuesContainer implements Serializable {

        private static final long serialVersionUID = 472142124257311224L;

        public int retry = MAX_RETRIES;
        public int retryWait = WAIT_MS;
    }
}
