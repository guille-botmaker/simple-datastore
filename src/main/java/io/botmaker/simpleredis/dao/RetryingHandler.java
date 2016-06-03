package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.exception.NoMoreRetriesException;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import io.botmaker.simpleredis.service.RedisServer;
import io.botmaker.simpleredis.service.SimpleDatastoreService;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
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
        return buildKey(entity.getEntityName(), entity.getId(), entity.usesAppIdPrefix(), redisServer);
    }

    private String buildKey(final String entityName, final String entityKey, final boolean entityUsesAppIdPrefix, final RedisServer redisServer) {
        return (entityUsesAppIdPrefix ? redisServer.getAppId() : "default") +
                ":" +
                entityName +
                ":" +
                entityKey;
    }

    private String buildIndexableKey(final PropertyMeta propertyMeta, final RedisEntity entity, final Object propertyValue, final RedisServer redisServer) {
        return
                (entity.usesAppIdPrefix() ? redisServer.getAppId() : "default") +
                        ":" +
                        entity.getEntityName() +
                        ":index:" +
                        propertyMeta.getPropertyName() +
                        ":" +
                        propertyValue;
    }

    private Map<String, PropertyMeta> buildIndexablePropertiesKeys(final RedisEntity entity, final RedisServer redisServer) {

        final List<PropertyMeta> indexableProperties = entity.getIndexableProperties();

        final HashMap<String, PropertyMeta> map = new HashMap<>(indexableProperties.size());
        indexableProperties.stream().forEach(ip -> map.put(buildIndexableKey(ip, entity, ip.get(), redisServer), ip));
        return map;
    }

    public <T extends RedisEntity> T tryDSGet(final String entityKey, final DAO<T> dao) {
        final T[] result = (T[]) new RedisEntity[1];

        final T sample = dao.getSample();

        tryClosure((redisServer, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGet", new Exception());
            }

            final String key = buildKey(sample.getEntityName(), entityKey, sample.usesAppIdPrefix(), redisServer);
            final String data;
            try (final Jedis jedis = redisServer.getPool().getResource()) {
                data = jedis.get(key);
            }

            result[0] = data == null ? null : dao.buildPersistentObjectInstanceFromPersistedStringData(data);

        }, result);

        return result[0];
    }

    public <T extends RedisEntity> List<T> tryDSGetAll(final DAO<T> dao) {
        final List<T> result = new ArrayList<>();

        tryClosure((redisServer, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetAll", new Exception());
            }

            final T sample = dao.buildPersistentObjectInstance();
            sample.setId("*");
            final String keyName = buildKey(sample, redisServer);

            final List<T> entities = getMultiple(redisServer, "keys", keyName, dao);
            if (entities != null) {
                result.addAll(entities);
            }
        }, null);

        return result;
    }

    public <T extends RedisEntity> Map<String, T> tryDSGetMultiple(final Collection<String> keys, final DAO<T> dao) {
        final Map<String, T> result = new HashMap<>();
        final T sample = dao.getSample();
        final String entityName = sample.getEntityName();
        final boolean usesAppIdPrefix = sample.usesAppIdPrefix();

        if (keys != null && !keys.isEmpty()) {
            tryClosure((redisServer, results, loggingActivated) -> {
                if (loggingActivated) {
                    LOGGER.log(Level.SEVERE, "PERF - tryDSGetMultiple", new Exception());
                }

                final String[] keysArray = keys.stream()
                        .map(id -> buildKey(entityName, id, usesAppIdPrefix, redisServer))
                        .toArray(String[]::new);

                final List<String> resultList;
                try (final Jedis jedis = redisServer.getPool().getResource()) {
                    resultList = jedis.mget(keysArray);
                }

                if (resultList != null) {
                    result.putAll(
                            resultList.stream()
                                    .map((Function<String, RedisEntity>) dao::buildPersistentObjectInstanceFromPersistedStringData)
                                    .collect(Collectors.toMap(RedisEntity::getId, i -> (T) i))
                    );
                }

            }, null);
        }
        return result;
    }

    public <T extends RedisEntity> List<T> tryDSGetByIndexableProperty(final String indexablePropertyName, final String key, final DAO<T> dao) {
        final List<T> result = new ArrayList<>(10);

        tryClosure((redisServer, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetByIndexableProperty", new Exception());
            }

            final T sample = dao.getSample();
            final String keyName = buildIndexableKey(sample.getIndexablePropertyByName(indexablePropertyName), sample, key, redisServer);
            final List<T> entities = getMultiple(redisServer, "smembers", keyName, dao);
            if (entities != null) {
                result.addAll(entities);
            }
        }, null);

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

            try (final Jedis jedis = redisServer.getPool().getResource()) {
                Pipeline pipeline = jedis.pipelined();

                pipeline.set(key, data);

                if (expiring > 0) {
                    pipeline.expire(key, expiring);
                }
                putAllEntityIndexes(buildIndexablePropertiesKeys(entity, redisServer), pipeline, key, expiring);
                pipeline.sync();
            }
        }, null);
    }

    public <T extends RedisEntity> void tryDSPutMultiple(final Collection<T> entities) {
        tryClosure((redisServer, results, loggingActivated) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSPutMultiple", new Exception());
            }

            try (final Jedis jedis = redisServer.getPool().getResource()) {
                final Pipeline pipeline = jedis.pipelined();

                entities.stream().forEach(entity -> {
                    final String key = buildKey(entity, redisServer);
                    final String data = entity.getDataObject().toString();
                    final int expiring = entity.getSecondsToExpire();

                    pipeline.set(key, data);

                    if (expiring > 0) {
                        pipeline.expire(key, expiring);
                    }
                    putAllEntityIndexes(buildIndexablePropertiesKeys(entity, redisServer), pipeline, key, expiring);
                });
                pipeline.sync();
            }
        }, null);
    }

    public void tryDSRemove(final String entityKey, final DAO dao) {
        final RedisEntity redisEntity = tryDSGet(entityKey, dao);

        if (redisEntity != null) {
            tryClosure((redisServer, results, loggingActivated) -> {
                if (loggingActivated) {
                    LOGGER.log(Level.SEVERE, "PERF - tryDSRemove", new Exception());
                }

                final Set<String> indexableProperties = buildIndexablePropertiesKeys(redisEntity, redisServer).keySet();
                final String theKey = buildKey(dao.getEntityName(), entityKey, dao.getSample().usesAppIdPrefix(), redisServer);

                try (final Jedis jedis = redisServer.getPool().getResource()) {
                    final Pipeline pipeline = jedis.pipelined();

                    pipeline.del(theKey);
                    indexableProperties.stream().forEach(pipeline::srem);

                    pipeline.sync();
                }
            }, null);
        }
    }

    public void tryDSRemove(final Collection<String> entityKeys, final DAO dao) {
        final Map<String, RedisEntity> map = tryDSGetMultiple(entityKeys, dao);

        if (!map.isEmpty()) {
            tryClosure((redisServer, results, loggingActivated) -> {
                if (loggingActivated) {
                    LOGGER.log(Level.SEVERE, "PERF - tryDSRemoveMultiple", new Exception());
                }

                try (final Jedis jedis = redisServer.getPool().getResource()) {
                    final Pipeline pipeline = jedis.pipelined();

                    map.values().stream().map(re -> buildIndexablePropertiesKeys(re, redisServer).keySet()).forEach(ip -> ip.stream().forEach(pipeline::del));
                    map.keySet().stream().forEach(pipeline::srem);

                    pipeline.sync();
                }
            }, null);
        }
    }

    private <T extends RedisEntity> List<T> getMultiple(final RedisServer redisServer, final String redisCommand, final String keyName, final DAO<T> dao) {

        final String script = "local keysArray = redis.call('" + redisCommand + "', KEYS[1])\n" +
                "if next(keysArray) == nil then\n" +
                "   return nil\n" +
                "else\n" +
                "   return redis.call('mget', unpack(keysArray))  end\n";

        List<String> resultList;
        try (final Jedis jedis = redisServer.getPool().getResource()) {
            resultList = (List<String>) jedis.eval(script, 1, keyName);
        }

        return resultList == null ? null : resultList.stream()
                .map(dao::buildPersistentObjectInstanceFromPersistedStringData)
                .collect(Collectors.toList());
    }

    private void putAllEntityIndexes(final Map<String, PropertyMeta> indexableProperties, final Pipeline pipeline, final String key, final int expiring) {

        indexableProperties.entrySet().stream().forEach(p -> {

            final String indexPropertyKey = p.getKey();

            final StringBuilder scriptStringBuilder = new StringBuilder(250);

            scriptStringBuilder.append("local key = redis.call('spop', KEYS[1])\n");
            scriptStringBuilder.append("redis.call('sadd', KEYS[1], ARGS[1])\n");
            if (expiring > 0) {
                scriptStringBuilder.append("redis.call('expire',indexPropertyKey, expiring) \n");
            }

            scriptStringBuilder.append("if key == nil then\n" +
                    "   return nil\n");

            if (p.getValue().isUniqueIndex()) {
                scriptStringBuilder.append("else\n" +
                        "   local entityToDelKey = redis.call('get', key)\n");
//                        "   redis.call('del', entityToDelKey)\n");
            }
            scriptStringBuilder.append("end");


            pipeline.eval(scriptStringBuilder.toString(), 1, indexPropertyKey, key);
        });
    }

//    private void deleteUniqueIndexesEntities(final Jedis jedis, final List<Response<String>> uniqueIndexesEntitiesToDelete) {
//        final Pipeline pipeline = jedis.pipelined();
//        final String[] uniqueIndexesEntitiesToDeleteArray = new String[uniqueIndexesEntitiesToDelete.size()];
//        uniqueIndexesEntitiesToDelete.stream().map(Response::get).collect(Collectors.toList()).toArray(uniqueIndexesEntitiesToDeleteArray);
//        pipeline.del(uniqueIndexesEntitiesToDeleteArray);
//        pipeline.sync();
//    }

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


//    public static void main(String[] args) {
//
//        final SimpleDatastoreService service = SimpleDatastoreServiceFactory.getSimpleDatastoreService();
//        service.configRedisServer("test", "104.197.121.7", "fSsVBcC5mDcG4c24vjSsQ33Ba2ZKbj7W52HYnK3bBZYFGGD8kjIzSBmc4w");
//        final RedisServer redisServer = service.getRedisServer();
//
//
//        List<String> resultList = null;
//        final String script =
//                "local keysArray = redis.call('smembers', KEYS[1])\n" +
//                        "if next(keysArray) == nil then\n" +
//                        "   return nil\n" +
//                        "else\n" +
//                        "   return redis.call('mget', unpack(keysArray))  end\n";
//
//        try (final Jedis jedis = redisServer.getPool().getResource()) {
//            //jedis.scriptKill();
//            System.err.println("" + jedis.eval(script, 1, "test:User:index:AGE:35"));
//        }
//        System.err.println("resultList " + resultList);
//    }
}
