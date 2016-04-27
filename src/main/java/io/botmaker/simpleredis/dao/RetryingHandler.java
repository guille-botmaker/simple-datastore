package io.botmaker.simpleredis.dao;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import io.botmaker.simpleredis.exception.NoMoreRetriesException;
import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.INT;
import io.botmaker.simpleredis.model.config.LONG;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import io.botmaker.simpleredis.model.config.STRING;
import io.botmaker.simpleredis.service.RedisServer;
import io.botmaker.simpleredis.service.SimpleDatastoreService;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.Serializable;
import java.lang.reflect.Field;
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

    public List<String> buildIndexablePropertiesKeys(final RedisEntity entity, final RedisServer redisServer) {
        return entity
                .getIndexableProperties().stream()
                .map(ip -> (entity.usesAppIdPrefix() ? (redisServer.getAppId() + ":") : "") + entity.getEntityName() + ":index:" + ip.getPropertyName())
                .collect(Collectors.toList());
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

            if (data == null) {
                result[0] = null;
            } else {
                final T foundResult = dao.buildPersistentObjectInstance();

                foundResult.setId(entityKey);
                foundResult.getDataObject().mergeWith(new DataObject(data));


                private final int secondsToExpire; // 0 means never
                private final boolean usesAppIdPrefix;

                private final Map<String, PropertyMeta> propertiesMetadata = new HashMap<>();
                // entity usefull properties

                @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
                public IntegerProperty GROUP_ID;

                @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
                public LongProperty LAST_MODIFICATION;

                public StringProperty OBJECT_TYPE;


                this.secondsToExpire = secondsToExpire;
                this.usesAppIdPrefix = usesAppIdPrefix;

                GROUP_ID = new INT(this).indexable().build();
                LAST_MODIFICATION = new LONG(this).sendToClient().mandatory().indexable().build();
                OBJECT_TYPE = new STRING(this).sendToClient().mandatory().build();

                for (final Field field : getClass().getFields()) {
                    if (PropertyMeta.class.isAssignableFrom(field.getType())) {
                        final String propertyName = field.getName();

                        try {
                            final PropertyMeta propertyMeta = (PropertyMeta) field.get(this);

                            propertyMeta.setPropertyName(propertyName);
                            addPropertyMeta(propertyName, propertyMeta);

                        } catch (final IllegalAccessException _illegalAccessException) {
                            throw new RuntimeException("Problems getting value for field [" + propertyName + "], of class [" + getClass() + "]. Possible private variable?: " + _illegalAccessException.getMessage(), _illegalAccessException);
                        }
                    }
                }


                result[0] = foundResult;
            }
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
