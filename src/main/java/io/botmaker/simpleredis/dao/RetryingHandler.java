package io.botmaker.simpleredis.dao;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import io.botmaker.simpleredis.exception.NoMoreRetriesException;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import io.botmaker.simpleredis.service.RedisServer;
import io.botmaker.simpleredis.service.SimpleDatastoreService;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    private String buildKey(final RedisEntity entity, final RedisServer redisServer) {
        return (entity.usesAppIdPrefix() ? (redisServer.getAppId() + ":") : "") + entity.getEntityName() + ":" + entity.getId();
    }

    private String buildIndexableKey(final PropertyMeta propertyMeta, final RedisEntity entity, final RedisServer redisServer) {
        return (entity.usesAppIdPrefix() ? (redisServer.getAppId() + ":") : "") + entity.getEntityName() + ":index:" + propertyMeta.getPropertyName();
    }

    private List<String> buildIndexablePropertiesKeys(final RedisEntity entity, final RedisServer redisServer) {
        return entity
                .getIndexableProperties().stream()
                .map(ip -> buildIndexableKey(ip, entity, redisServer))
                .collect(Collectors.toList());
    }

    public <T extends RedisEntity> T tryDSGet(final String entityKey, final DAO<T> dao) {
        final T[] result = (T[]) new RedisEntity[1];

        tryClosure((redisServer, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGet", new Exception());
            }

            final String data;
            try (final Jedis jedis = redisServer.getPool().getResource()) {
                data = jedis.get(entityKey);
            }

            result[0] = data == null ? null : dao.buildPersistentObjectInstanceFromPersistedStringData(data);

        }, result);

        return result[0];
    }

    public <T extends RedisEntity> T tryDSGetByIndexableProperty(final String indexablePropertyName, final String key, final DAO<T> dao) {
        final T[] result = (T[]) new RedisEntity[1];

        tryClosure((redisServer, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetByIndexableProperty", new Exception());
            }

            final T sample = dao.getSample();
            final String keyName = buildIndexableKey(sample.getIndexablePropertyByName(indexablePropertyName), sample, redisServer);
            final String data;
            final String script =
                    "local the_key = redis.call('get', KEYS[1])\n" +
                            "if the_key then\n" +
                            "return redis.call('get', the_key)\n" +
                            "else\n" +
                            "return nil end";

            try (final Jedis jedis = redisServer.getPool().getResource()) {
                data = (String) jedis.eval(script, 1, keyName);
            }

            result[0] = data == null ? null : dao.buildPersistentObjectInstanceFromPersistedStringData(data);

        }, result);

        return result[0];
    }

    public void tryDSRemove(final Key entityKey) {
        tryClosure((datastore, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSRemove", new Exception());
            }

            datastore.delete(entityKey);
        }, null);
    }

    public void tryDSRemove(final Collection<String> entityKeys) {
        final String key = buildKey(entity, redisServer);
        final List<String> indexableProperties = buildIndexablePropertiesKeys(entity, redisServer);

        try (final Jedis jedis = redisServer.getPool().getResource()) {
            final Pipeline pipeline = jedis.pipelined();

            pipeline.set(key, data);

            if (expiring > 0) {
                pipeline.expire(key, expiring);
            }

            indexableProperties.stream().forEach(p -> {
                pipeline.set(p, data);

                if (expiring > 0) {
                    pipeline.expire(p, expiring);
                }
            });
            pipeline.sync();
        }


        tryClosure((datastore, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSRemoveMultiple", new Exception());
            }

            datastore.delete(entityKeys);
        }, null);
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

            final String key = buildKey(entity, redisServer);
            final String data = entity.getDataObject().toString();
            final int expiring = entity.getSecondsToExpire();
            final List<String> indexableProperties = buildIndexablePropertiesKeys(entity, redisServer);

            try (final Jedis jedis = redisServer.getPool().getResource()) {
                final Pipeline pipeline = jedis.pipelined();

                pipeline.set(key, data);

                if (expiring > 0) {
                    pipeline.expire(key, expiring);
                }

                indexableProperties.stream().forEach(p -> {
                    pipeline.set(p, key);

                    if (expiring > 0) {
                        pipeline.expire(p, expiring);
                    }
                });
                pipeline.sync();
            }
        }, null);
    }

    private void tryClosure(final Closure closure, final Object[] results) {
        final ValuesContainer values = new ValuesContainer();
        final SimpleDatastoreService simpleDatastoreService = SimpleDatastoreServiceFactory.getSimpleDatastoreService();
        final RedisServer redisServer = simpleDatastoreService.getRedisServer();
        final boolean loggingActivated = simpleDatastoreService.isDatastoreCallsLoggingActivated();

        while (true) {
            try {
                closure.execute(redisServer, results, loggingActivated);
                break;
            } catch (final Exception exception) {
                handleError(values, exception, false);
            }
        }
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

    public static final class ValuesContainer implements Serializable {

        private static final long serialVersionUID = 472142124257311224L;

        public int retry = MAX_RETRIES;
        public int retryWait = WAIT_MS;
    }
}
