package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.audit.SpanTracing;
import io.botmaker.simpleredis.exception.NoMoreRetriesException;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import io.botmaker.simpleredis.service.RedisServer;
import io.botmaker.simpleredis.service.SimpleDatastoreService;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import io.botmaker.simpleredis.util.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

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

    public static final String DEFAULT = "prod";
    private static final long serialVersionUID = 472842924253314234L;
    private static final Logger LOGGER = Logger.getLogger(RetryingHandler.class.getName());
    private static final int MAX_RETRIES = 4;
    private static final int WAIT_MS = 200;

//    public static void main(String[] args) {
//        final RedisServer redisServer1 = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getRedisServer();
//        redisServer1.configure("35.230.113.99", 6379, "", null);
//
//        final RetryingHandler retryingHandler = new RetryingHandler();
//        retryingHandler.tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
//            final String keyName = "prod:RuleBuilder:*";
//            final List<String> keys = retryingHandler.executeRedisLuaCommandForMultipleRawEntities(redisServer, "keys", Collections.singletonList(keyName), Collections.emptyList());
//            System.err.println(keys);
//        }, null);
//
//        System.exit(0);
//    }

    public static void sleep(final int millis) {
        try {
            Thread.sleep(millis);

        } catch (final InterruptedException ie) {
            // nothing to do
        }
    }

    public static String buildKey(final String entityName, final String entityKey, final boolean entityUsesAppIdPrefix, final boolean isProductionEnvironment,
                                  final RedisServer redisServer) {

        return (entityUsesAppIdPrefix ? redisServer.getAppId() : (isProductionEnvironment ? DEFAULT : redisServer.getAppId())) +
                ":" +
                entityName +
                ":" +
                entityKey;
    }

    private String buildKey(final DAO dao, final RedisEntity entity, final boolean isProductionEnvironment, final RedisServer redisServer) {
        return buildKey(dao.getEntityName(), entity.getId(), entity.usesAppIdPrefix(), isProductionEnvironment, redisServer);
    }

    private String buildIndexableKey(final PropertyMeta propertyMeta, final DAO dao, final RedisEntity entity, final boolean isProductionEnvironment, final Object propertyValue, final RedisServer redisServer) {
        return (entity.usesAppIdPrefix() ? redisServer.getAppId() : (isProductionEnvironment ? DEFAULT : redisServer.getAppId())) +
                ":" +
                dao.getEntityName() +
                "Indexes:" +
                propertyMeta.getPropertyName() +
                ":" +
                propertyValue;
    }

    private List<IndexablePropertyInfoContainer> buildIndexablePropertiesKeys(final String entityKey, final RedisEntity entity, final DAO dao,
                                                                              final boolean isProductionEnvironment, final RedisServer redisServer) {

        final List<PropertyMeta> indexableProperties = entity.getIndexableProperties();

        final List<IndexablePropertyInfoContainer> list = new ArrayList<>(indexableProperties.size());
        indexableProperties.forEach(ip -> list.add(new IndexablePropertyInfoContainer(entityKey, buildIndexableKey(ip, dao, entity, isProductionEnvironment, ip.get(), redisServer), ip)));
        return list;
    }

    private String tryDSRawGet(final String entityName, final String entityKey, final boolean usesAppIdPrefix) {

        final String[] result = new String[1];
        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSRawGet", new Exception());
            }

            final String key = buildKey(entityName, entityKey, usesAppIdPrefix, isProductionEnvironment, redisServer);
            final String data;
            try (final Jedis jedis = redisServer.getPool().getResource()) {
                data = jedis.get(key);
            }

            result[0] = data;

        }, result);

        return result[0];
    }

    <T extends RedisEntity> T tryDSGet(final String entityKey, final DAO<T> dao) {
        final String data = tryDSRawGet(dao.getEntityName(), entityKey, dao.getSample().usesAppIdPrefix());
        return data == null ? null : dao.buildPersistentObjectInstanceFromPersistedStringData(data);
    }

    <T extends RedisEntity> List<T> tryDSGetAll(final DAO<T> dao) {
        final List<T> result = new ArrayList<>();

        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetAll", new Exception());
            }

            final T sample = dao.buildPersistentObjectInstance();
            sample.setId("*");

            final String keyName = buildKey(dao, sample, isProductionEnvironment, redisServer);
            final List<T> entities = executeRedisLuaCommandForMultipleEntities(dao, redisServer, "keys", Collections.singletonList(keyName));
            if (entities != null) {
                result.addAll(entities);
            }
        }, null);

        return result;
    }

    <T extends RedisEntity> Map<String, T> tryDSGetMultiple(final Collection<String> keys, final DAO<T> dao) {
        final Map<String, T> result = new HashMap<>();
        final T sample = dao.getSample();
        final String entityName = dao.getEntityName();
        final boolean usesAppIdPrefix = sample.usesAppIdPrefix();

        if (keys != null && !keys.isEmpty()) {
            tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
                if (loggingActivated) {
                    LOGGER.log(Level.SEVERE, "PERF - tryDSGetMultiple", new Exception());
                }

                final String[] keysArray = keys.stream()
                        .map(id -> buildKey(entityName, id, usesAppIdPrefix, isProductionEnvironment, redisServer))
                        .toArray(String[]::new);

                final List<String> resultList;
                try (final Jedis jedis = redisServer.getPool().getResource()) {
                    resultList = jedis.mget(keysArray);
                }

                if (resultList != null) {
                    result.putAll(
                            resultList.stream()
                                    .filter(Objects::nonNull)
                                    .map((Function<String, RedisEntity>) dao::buildPersistentObjectInstanceFromPersistedStringData)
                                    .collect(Collectors.toMap(RedisEntity::getId, i -> (T) i))
                    );
                }

            }, null);
        }
        return result;
    }

    <T extends RedisEntity> List<T> tryDSGetByIndexableProperty(final String indexablePropertyName, final String key, final DAO<T> dao) {
        final List<T> result = new ArrayList<>();

        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetByIndexableProperty", new Exception());
            }

            final T sample = dao.getSample();
            final String keyName = buildIndexableKey(sample.getIndexablePropertyByName(indexablePropertyName), dao, sample, isProductionEnvironment, key, redisServer);
            final List<T> entities = executeRedisLuaCommandForMultipleEntities(dao, redisServer, "smembers", Collections.singletonList(keyName));
            if (entities != null) {
                result.addAll(entities);
            }
        }, null);

        return result;
    }

    <T extends RedisEntity> List<T> tryDSGetLastOccurrencesByIndexableProperty(final String indexablePropertyName, final String key,
                                                                               final int ocurrences, final DAO<T> dao) {

        return tryDSGetFromToByIndexableProperty(indexablePropertyName, key, -ocurrences, -1, dao);
    }


    <T extends RedisEntity> List<T> tryDSGetFromToByIndexableProperty(final String indexablePropertyName, final String key,
                                                                      final int from, final int to, final DAO<T> dao) {
        final List<T> result = new ArrayList<>(200);

        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetLastOccurrencesByIndexableProperty", new Exception());
            }

            final T sample = dao.getSample();
            final String keyName = indexablePropertyNameToSortedSet(
                    buildIndexableKey(sample.getIndexablePropertyByName(indexablePropertyName), dao, sample, isProductionEnvironment, key, redisServer)
            );

            final List<String> range = new ArrayList<>(2);
            range.add(String.valueOf(from));
            range.add(String.valueOf(to));
            final List<T> entities = executeRedisLuaCommandForMultipleEntities(dao, redisServer, "zrange", Collections.singletonList(keyName), range);
            if (entities != null) {
                result.addAll(entities);
            }


//            final List<String> data;
//            final String keys = buildLuaParameters("KEYS", keyNames);
//            String args = buildLuaParameters("ARGV", argNames);
//            args = (args.length() > 0 ? "," : "") + args;
//
//            final String script = "local keysArray = redis.call('zrange', " + keys + args + ")\n" +
//                    "if next(keysArray) == nil then\n" +
//                    "   return nil\n" +
//                    "else\n" +
//                    "   return redis.call('mget', unpack(keysArray))  end\n";
//
//            try (final Jedis jedis = redisServer.getPool().getResource()) {
//                {String.valueOf(-ocurrences), String.valueOf(-1)}
//                data = (List<String>) jedis.eval(script, keyName, );
//            }
//
//            data = jedis.zrange(keyName, -ocurrences, -1);
//
//            if (data != null && !data.isEmpty()) {
//                final List<T> entities = instantiateEntities(dao, new ArrayList<>(data));
//                if (entities != null) {
//                    result.addAll(entities);
//                }
//            }
        }, null);

        return result;
    }

    <T extends RedisEntity> List<T> tryDSGetIntersectionOfIndexableProperties(final DAO<T> dao, final Map<String, String> propertyNameAndValueMap) {
        final List<T> result = new ArrayList<>(10);

        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetIntersectionOfIndexableProperties", new Exception());
            }

            final T sample = dao.getSample();
            final List<String> keyNames = propertyNameAndValueMap.entrySet().stream().
                    map(entry ->
                            buildIndexableKey(sample.getIndexablePropertyByName(entry.getKey()), dao, sample, isProductionEnvironment, entry.getValue(), redisServer)).
                    collect(Collectors.toList());

            result.addAll(executeRedisLuaCommandForMultipleEntities(dao, redisServer, "SINTER", keyNames));
        }, null);

        return result;
    }

    <T extends RedisEntity> List<T> tryDSGetUnionOfIndexableProperties(final DAO<T> dao, final List<Pair<String, String>> propertyNameAndValuePair) {
        final List<T> result = new ArrayList<>(10);

        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSGetUnionOfIndexableProperties", new Exception());
            }

            final T sample = dao.getSample();
            final List<String> keyNames = propertyNameAndValuePair.stream().
                    map(entry ->
                            buildIndexableKey(sample.getIndexablePropertyByName(entry.getKey()), dao, sample, isProductionEnvironment, entry.getValue(), redisServer)).
                    collect(Collectors.toList());

            result.addAll(executeRedisLuaCommandForMultipleEntities(dao, redisServer, "SUNION", keyNames));
        }, null);

        return result;
    }

    void tryDSPut(final DAO dao, final RedisEntity entity) {
        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSPut", new Exception());
            }

            final String key = buildKey(dao, entity, isProductionEnvironment, redisServer);
            final String data = entity.getDataObject().toString();
            final int expiring = entity.getSecondsToExpire();

            // try to remove old entity if exists with their indexable properties
            tryDSRemove(entity.getId(), dao, true);

            final List<IndexablePropertyInfoContainer> indexableProperties = buildIndexablePropertiesKeys(key, entity, dao, isProductionEnvironment, redisServer);

            try (final Jedis jedis = redisServer.getPool().getResource()) {
                // remove old entities that have the same unique indexable property
                removeOldEntitiesThatHaveTheSameUniqueIndexablePropertyValue(dao, redisServer, jedis, indexableProperties, isProductionEnvironment);

                final Pipeline pipeline = jedis.pipelined();
                pipeline.set(key, data);
                if (expiring > 0) {
                    pipeline.expire(key, expiring);
                }

                indexableProperties.forEach(e -> {
                    pipeline.sadd(e.propertyKey, key);

                    final String sortedSetKey = indexablePropertyNameToSortedSet(e.propertyKey);
                    pipeline.zadd(sortedSetKey, entity.getSortingScore(), key);

                    if (expiring > 0) {
                        pipeline.expire(e.propertyKey, expiring);
                        pipeline.expire(sortedSetKey, expiring);
                    }
                });
                pipeline.sync();
            }
        }, null);
    }

//    public void tryDSRawPut(final String data, final int expiring, final String entityName, final String entityKey, final boolean usesAppIdPrefix) {
//        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
//            if (loggingActivated) {
//                LOGGER.log(Level.SEVERE, "PERF - tryDSRawPut", new Exception());
//            }
//
//            final String key = buildKey(entityName, entityKey, usesAppIdPrefix, isProductionEnvironment, redisServer);
//
//            try (final Jedis jedis = redisServer.getPool().getResource()) {
//
//                final Pipeline pipeline = jedis.pipelined();
//                pipeline.set(key, data);
//                if (expiring > 0) {
//                    pipeline.expire(key, expiring);
//                }
//            }
//        }, null);
//    }

    <T extends RedisEntity> void tryDSPutMultiple(final DAO dao, final Collection<T> entities) {
        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSPutMultiple", new Exception());
            }

            // try to remove old entities if exist with their indexable properties
            tryDSRemove(entities.stream().map(RedisEntity::getId).collect(Collectors.toList()), dao, true);

            final List<IndexablePropertyInfoContainer> indexableProperties = new ArrayList<>(entities.size());
            entities.forEach(e -> indexableProperties.addAll(buildIndexablePropertiesKeys(buildKey(dao, e, isProductionEnvironment, redisServer), e, dao,
                    isProductionEnvironment, redisServer)));

            try (final Jedis jedis = redisServer.getPool().getResource()) {
                // remove old entities that have the same unique indexable property
                removeOldEntitiesThatHaveTheSameUniqueIndexablePropertyValue(dao, redisServer, jedis, indexableProperties, isProductionEnvironment);

                final Pipeline pipeline = jedis.pipelined();
                entities.forEach(entity -> {
                    final String key = buildKey(dao, entity, isProductionEnvironment, redisServer);
                    final String data = entity.getDataObject().toString();
                    final int expiring = entity.getSecondsToExpire();

                    pipeline.set(key, data);

                    if (expiring > 0) {
                        pipeline.expire(key, expiring);
                    }

                    indexableProperties.forEach(ip -> {
                        pipeline.sadd(ip.propertyKey, ip.entityKey);

                        final String sortedSetKey = indexablePropertyNameToSortedSet(ip.propertyKey);
                        pipeline.zadd(sortedSetKey, entity.getSortingScore(), ip.entityKey);

                        if (expiring > 0) {
                            pipeline.expire(ip.propertyKey, expiring);
                            pipeline.expire(sortedSetKey, expiring);
                        }
                    });
                });
                pipeline.sync();
            }
        }, null);
    }

//    public void tryDSRawPutMultiple(final Map<String, String> datas, final int expiring, final String entityName, final boolean usesAppIdPrefix) {
//        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
//            if (loggingActivated) {
//                LOGGER.log(Level.SEVERE, "PERF - tryDSRawPutMultiple", new Exception());
//            }
//
//            try (final Jedis jedis = redisServer.getPool().getResource()) {
//
//                final Pipeline pipeline = jedis.pipelined();
//                datas.forEach((key1, value) -> {
//                    final String key = buildKey(entityName, key1, usesAppIdPrefix, isProductionEnvironment, redisServer);
//                    pipeline.set(key, value);
//
//                    if (expiring > 0) {
//                        pipeline.expire(key, expiring);
//                    }
//                });
//                pipeline.sync();
//            }
//        }, null);
//    }

    void tryDSRemove(final String entityId, final DAO dao) {

        tryDSRemove(entityId, dao, false);
    }

    private void tryDSRemove(final String entityId, final DAO dao, final boolean onlyProperties) {
        final RedisEntity redisEntity = tryDSGet(entityId, dao);

        if (redisEntity != null) {
            tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
                if (loggingActivated) {
                    LOGGER.log(Level.SEVERE, "PERF - tryDSRemove", new Exception());
                }

                final String theKey = buildKey(dao.getEntityName(), entityId, dao.getSample().usesAppIdPrefix(), isProductionEnvironment, redisServer);
                final List<IndexablePropertyInfoContainer> indexableProperties = buildIndexablePropertiesKeys(theKey, redisEntity, dao, isProductionEnvironment, redisServer);

                try (final Jedis jedis = redisServer.getPool().getResource()) {
                    final Pipeline pipeline = jedis.pipelined();

                    if (!onlyProperties) {
                        pipeline.del(theKey);
                    }
                    indexableProperties.forEach(ip -> {
                        pipeline.srem(ip.propertyKey, ip.entityKey);
                        pipeline.zrem(indexablePropertyNameToSortedSet(ip.propertyKey), ip.entityKey);
                    });

                    pipeline.sync();
                }
            }, null);
        }
    }

//    public void tryDSRawRemove(final String entityName, final String entityKey, final boolean usesAppIdPrefix) {
//        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
//            if (loggingActivated) {
//                LOGGER.log(Level.SEVERE, "PERF - tryDSRawRemove", new Exception());
//            }
//
//            final String key = buildKey(entityName, entityKey, usesAppIdPrefix, isProductionEnvironment, redisServer);
//            try (final Jedis jedis = redisServer.getPool().getResource()) {
//                jedis.del(key);
//            }
//        }, null);
//    }


    void tryDSRemove(final Collection<String> entityIds, final DAO dao) {

        tryDSRemove(entityIds, dao, false);
    }

    private void tryDSRemove(final Collection<String> entityIds, final DAO dao, final boolean onlyProperties) {
        final Map<String, RedisEntity> map = tryDSGetMultiple(entityIds, dao);

        if (!map.isEmpty()) {
            tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
                if (loggingActivated) {
                    LOGGER.log(Level.SEVERE, "PERF - tryDSRemove", new Exception());
                }

                try (final Jedis jedis = redisServer.getPool().getResource()) {
                    final Pipeline pipeline = jedis.pipelined();


                    final Map<RedisEntity, String> entitiesWithKey = new HashMap<>(entityIds.size());
                    map.values().forEach(e -> entitiesWithKey.put(e, buildKey(dao, e, isProductionEnvironment, redisServer)));

                    if (!onlyProperties) {
                        entitiesWithKey.values().forEach(pipeline::del);
                    }
                    entitiesWithKey.forEach((key, value) -> buildIndexablePropertiesKeys(value, key, dao, isProductionEnvironment, redisServer).
                            forEach(ip -> {
                                pipeline.srem(ip.propertyKey, ip.entityKey);
                                pipeline.zrem(indexablePropertyNameToSortedSet(ip.propertyKey), ip.entityKey);
                            }));

                    pipeline.sync();
                }
            }, null);
        }
    }

    public List<String> tryDSOrderedSetGetUnion(final String entityName, final List<String> entityKeys, final int quantity, final boolean usesAppIdPrefix) {
        final List[] entities = new List[1];
        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSOrderedSetGetUnion", new Exception());
            }

            final String entityPrefix = buildKey(entityName, "", usesAppIdPrefix, isProductionEnvironment, redisServer);
            final String[] entityKeysArray = new String[entityKeys.size()];
            final int[] i = {0};
            entityKeys.forEach(k -> entityKeysArray[i[0]++] = entityPrefix + k + "Set");
            final String setResultKey = entityPrefix + "result" + RandomUtils.getInstance().getRandomSafeAlphaNumberString(5);

            try (final Jedis jedis = redisServer.getPool().getResource()) {
                final Pipeline pipelined = jedis.pipelined();

                pipelined.zunionstore(setResultKey, entityKeysArray);
                pipelined.expire(setResultKey, 30);
                pipelined.sync();
            }

            entities[0] = executeRedisLuaCommandForMultipleRawEntities(redisServer, "ZREVRANGE", Collections.singletonList(setResultKey), Arrays.asList("0", "" + (quantity - 1)));
        }, null);
        return entities[0];
    }

    public void tryDSOrderedSetPut(final long score, final String entityKey, final String data, final String entityName, final boolean usesAppIdPrefix) {
        tryClosure((redisServer, results, loggingActivated, isProductionEnvironment) -> {
            if (loggingActivated) {
                LOGGER.log(Level.SEVERE, "PERF - tryDSOrderedSetPut", new Exception());
            }

            final String setKeyName = (usesAppIdPrefix ? redisServer.getAppId() : (isProductionEnvironment ? DEFAULT : redisServer.getAppId())) +
                    ":" +
                    entityName +
                    "Set";

            final String keyName = buildKey(entityName, entityKey, usesAppIdPrefix, isProductionEnvironment, redisServer);
            try (final Jedis jedis = redisServer.getPool().getResource()) {
                final Pipeline pipelined = jedis.pipelined();
                pipelined.zadd(setKeyName, score, keyName);
                pipelined.set(keyName, data);
                pipelined.sync();
            }

        }, null);
    }

    public void tryClosure(final Closure closure, final Object[] results) {
        final ValuesContainer values = new ValuesContainer();
        final SimpleDatastoreService simpleDatastoreService = SimpleDatastoreServiceFactory.getSimpleDatastoreService();
        final RedisServer redisServer = simpleDatastoreService.getRedisServer();
        final boolean loggingActivated = simpleDatastoreService.isDatastoreCallsLoggingActivated();
        final boolean isProductionEnvironment = simpleDatastoreService.isProductionEnvironment();
        final SpanTracing spanTracing = simpleDatastoreService.getSpanTracing();

        while (true) {
            try {
                final Object span = spanTracing.startSpan(closure.getClass().getName());
                closure.execute(redisServer, results, loggingActivated, isProductionEnvironment);
                spanTracing.endSpan(span);
                break;
            } catch (final Exception exception) {
                handleError(values, exception);
            }
        }
    }

    private <T extends RedisEntity> List<T> executeRedisLuaCommandForMultipleEntities(final DAO<T> dao, final RedisServer redisServer, final String redisCommand, final List<String> keyNames) {

        return executeRedisLuaCommandForMultipleEntities(dao, redisServer, redisCommand, keyNames, Collections.emptyList());
    }

    private <T extends RedisEntity> List<T> executeRedisLuaCommandForMultipleEntities(final DAO<T> dao, final RedisServer redisServer, final String redisCommand, final List<String> keyNames, final List<String> argNames) {

        return instantiateEntities(dao, executeRedisLuaCommandForMultipleRawEntities(redisServer, redisCommand, keyNames, argNames));
    }

    private List<String> executeRedisLuaCommandForMultipleRawEntities(final RedisServer redisServer, final String redisCommand, final List<String> keyNames, final List<String> argNames) {
        final String keys = buildLuaParameters("KEYS", keyNames);
        String args = buildLuaParameters("ARGV", argNames);
        args = (args.length() > 0 ? "," : "") + args;

        final int BATCH_COUNT = 15;
        final int BATCH_SIZE = 5000;
        final int LIMIT = BATCH_COUNT * BATCH_SIZE;

        final String script =
                "local subrange = function(t, first, last)\n" +
                        "   local sub = {}\n" +
                        "   for i=first,last do\n" +
                        "       sub[#sub + 1] = t[i]\n" +
                        "   end\n" +
                        "   return sub\n" +
                        "end\n" +
                        "" +
                        "local keysArray = redis.call('" + redisCommand + "', " + keys + args + ")\n" +
                        "if next(keysArray) == nil then\n" +
                        "   return nil\n" +
                        "else\n" +
                        "   local all = {}\n" +
                        "   local n = 1\n" +
                        "   for i=0," + (BATCH_COUNT - 1) + " do\n" +
                        "       local batchKeys = subrange(keysArray, n, n + " + (BATCH_SIZE - 1) + ")\n " +
                        "       if next(batchKeys) ~= nil then \n" +
                        "           local batchResults = redis.call('mget', unpack(batchKeys))\n" +
                        "           if next(batchResults) ~= nil then \n" +
                        "               for _,v in ipairs(batchResults) do all[n]=v; n=n+1; end\n" +
                        "           end\n" +
                        "       end\n" +
                        "   end\n" +
                        "   return all\n" +
                        "end\n";


        List<String> resultList;
        final SpanTracing spanTracing = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getSpanTracing();

        try (final Jedis jedis = redisServer.getPool().getResource()) {

            final Object span = spanTracing.startSpan(redisCommand);

            resultList = (List<String>) jedis.eval(script, keyNames, argNames);

            spanTracing.endSpan(span);
        }

        if (resultList == null) return Collections.emptyList();

        if (resultList.size() >= LIMIT)
            throw new RuntimeException("ERROR more than [" + LIMIT + "] entities on redis query!");

        return resultList;
    }

    private String buildLuaParameters(final String name, final List<String> values) {
        final StringBuilder keyLuaParameters = new StringBuilder();
        for (int i = 1; i <= values.size(); i++) {
            keyLuaParameters.append(name).append("[").append(i).append("], ");
        }

        if (keyLuaParameters.length() > 0) {
            keyLuaParameters.delete(keyLuaParameters.length() - 2, keyLuaParameters.length());
        }
        return keyLuaParameters.toString();
    }

    private <T extends RedisEntity> List<T> instantiateEntities(final DAO<T> dao, final List<String> resultList) {
        return resultList == null ? Collections.emptyList() : resultList.stream()
                .filter(Objects::nonNull)
                .map(dao::buildPersistentObjectInstanceFromPersistedStringData)
                .collect(Collectors.toList());
    }

    private void removeOldEntitiesThatHaveTheSameUniqueIndexablePropertyValue(final DAO dao, final RedisServer redisServer, final Jedis jedis,
                                                                              final List<IndexablePropertyInfoContainer> indexableProperties, final boolean isProductionEnvironment) {
        // IMPORTANT NOTE: there is no possibility of put this in a LUA script,
        // because LUA script wont allow "spop" command!

        // remove the index key, so it remains unique
        final Pipeline pipelined = jedis.pipelined();
        final Map<Response<String>, List<IndexablePropertyInfoContainer>> responses = new HashMap<>(indexableProperties.size());
        for (final IndexablePropertyInfoContainer indexableProperty : indexableProperties) {
            final PropertyMeta propertyMeta = indexableProperty.property;
            if (propertyMeta.isIndexable() && propertyMeta.isUniqueIndex()) {
                responses.put(pipelined.spop(indexableProperty.propertyKey), indexableProperties);
            }
        }
        pipelined.sync();

        final List<String> entitiesToDelete = responses.keySet().stream().map(Response::get).
                filter(Objects::nonNull).collect(Collectors.toList());
        if (entitiesToDelete.size() > 0) {

            // delete the old entities referenced by old unique index key
            final String[] entitiesToDeleteArray = new String[entitiesToDelete.size()];
            entitiesToDelete.toArray(entitiesToDeleteArray);
            final List<RedisEntity> oldEntities = instantiateEntities(dao, jedis.mget(entitiesToDeleteArray));

            final Pipeline pipelined2 = jedis.pipelined();
            pipelined2.del(entitiesToDeleteArray);

            //delete keys from non unique indexable properties that reference to the old entity key
            final int[] index = {0};
            oldEntities.forEach(e -> buildIndexablePropertiesKeys(entitiesToDeleteArray[index[0]++], e, dao, isProductionEnvironment, redisServer).
                    forEach(indexablePropertyInfoContainer -> {
                                final PropertyMeta propertyMeta = indexablePropertyInfoContainer.property;
                                if (propertyMeta.isIndexable() && !propertyMeta.isUniqueIndex()) {
                                    pipelined2.srem(indexablePropertyInfoContainer.propertyKey, indexablePropertyInfoContainer.entityKey);
                                    pipelined2.zrem(indexablePropertyNameToSortedSet(indexablePropertyInfoContainer.propertyKey), indexablePropertyInfoContainer.entityKey);
                                }
                            }
                    ));
            pipelined2.sync();
        }
    }

    private String indexablePropertyNameToSortedSet(final String indexablePropertyName) {
        final int index = indexablePropertyName.lastIndexOf(':');
        return indexablePropertyName.substring(0, index) + "_SORTED:" + indexablePropertyName.substring(index + 1);
    }

    private void handleError(final ValuesContainer values, final Exception exception) {
        values.retry = values.retry - 1;

        if (values.retry <= 0) {
            LOGGER.log(Level.SEVERE, "PERF - No more tries for datastore access: " + exception.getMessage(), exception);
            throw new NoMoreRetriesException(exception);
        }

        sleep(values.retryWait);
    }

    public interface Closure {

        void execute(final RedisServer redisServer, final Object[] results, final boolean loggingActivated, final boolean isProductionEnvironment);
    }

    public static final class ValuesContainer implements Serializable {

        private static final long serialVersionUID = 472142124257311224L;

        int retry = MAX_RETRIES;
        int retryWait = WAIT_MS;
    }

    public static final class IndexablePropertyInfoContainer {

        public final PropertyMeta property;
        final String entityKey;
        final String propertyKey;

        IndexablePropertyInfoContainer(final String entityKey, final String propertyKey, final PropertyMeta property) {
            this.entityKey = entityKey;
            this.propertyKey = propertyKey;
            this.property = property;
        }
    }
}
