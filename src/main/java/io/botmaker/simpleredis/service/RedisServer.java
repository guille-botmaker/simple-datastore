package io.botmaker.simpleredis.service;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

import java.util.Collections;
import java.util.List;

public final class RedisServer {

//    private static final Logger LOGGER = Logger.getLogger(RedisServer.class.getName());

    private Pool<Jedis> pool;
    //    private final JedisSentinelPool pool;
    private String appId;

    public void configure(final String redisHost, final int redisPort, final String appId, final String redisAuthPassword) {
        this.appId = appId;

        final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(40);
        config.setMaxIdle(10);
        config.setTestWhileIdle(true);
        config.setTestOnCreate(false);

        pool = new JedisPool(config, redisHost, redisPort, 10000, redisAuthPassword);
//        pool = new JedisSentinelPool(MASTER_NAME, Collections.singleton(new HostAndPort(host, port).toString()), config, 2000);
    }

    public void configureSentinel(final String sentinelHost, final int sentinelPort, final String masterName, final String appId, final String redisAuthPassword,
                                  final List<ImmutablePair<ImmutablePair<String, Integer>, ImmutablePair<String, Integer>>> addressTranslators) {
        this.appId = appId;

        final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(40);
        config.setMaxIdle(10);
        config.setTestWhileIdle(true);
        config.setTestOnCreate(false);

        HostAndPort.setAddressTranslator(addressTranslators);

        pool = new JedisSentinelPool(masterName, Collections.singleton(sentinelHost + ":" + sentinelPort), config, 10000);
    }

    public Pool<Jedis> getPool() {
        return pool;
    }

    public String getAppId() {
        return appId;
    }

//    public T call(final RedisClosure redisClosure) {
////        if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
////            return null;
////        }
//        final Object[] out = new Object[1];
//
//        final RetryingExecutor executor = new RetryingExecutor(5, 200, new IClosure() {
//            @Override
//            public void execute(final Object params) throws Exception {
//                out[0] = doCallImpl(redisClosure);
//            }
//        }, null);
//
//        try {
//            executor.startExecution();
//        } catch (final Throwable throwable) {
//            final String msg = "Problems accessing Redis Server: " + throwable.getMessage();
//            LOGGER.log(Level.SEVERE, msg, throwable);
//            ExceptionUtils.report(throwable);
//
//            throw new RuntimeException(msg, throwable);
//        }
//        return (T) out[0];
//    }
//
//    public boolean tryToRunInsideSafeLock(final String lockKey, final int lockTimeoutInSeconds, final Runnable runnable) {
//
//        if (!tryToGetLock(lockKey, lockTimeoutInSeconds)) {
//            return false;
//        }
//
//        runnable.run();
//
//        releaseLock(lockKey);
//        return true;
//    }
//
//    private boolean tryToGetLock(final String key, final int lockTimeoutInSeconds) {
//        // REFERENCE: http://redis.io/commands/setnx (distributed option: http://redis.io/topics/distlock)
//        final long serverTime = getServerTime();
//        final String timeout = String.valueOf(serverTime + lockTimeoutInSeconds);
//
//        final Long returnValue = (Long) call(new RedisClosure() {
//            @Override
//            public Long execute(final Jedis jedis, final String appIds) {
//                return jedis.setnx(appIds + key, timeout);
//            }
//        });
//
//        // lock acquire if 1
//        if (returnValue == 1) {
//            return true;
//        }
//
//        String currentLockTimeoutString = ((RedisServer<String>) RedisServer.getInstance()).call(new RedisClosure() {
//            @Override
//            public String execute(final Jedis jedis, final String appIds) {
//                return jedis.get(appIds + key);
//            }
//        });
//
//        // lock is set if it is NOT timeouted
//        if (currentLockTimeoutString != null && Long.parseLong(currentLockTimeoutString) > serverTime) {
//            return false;
//        }
//
//        currentLockTimeoutString = ((RedisServer<String>) RedisServer.getInstance()).call(new RedisClosure() {
//            @Override
//            public String execute(final Jedis jedis, final String appIds) {
//                return jedis.getSet(appIds + key, timeout);
//            }
//        });
//
//        // lock acquire if the current lock is timeouted
//        return currentLockTimeoutString != null && Long.parseLong(currentLockTimeoutString) <= serverTime;
//    }
//
//    private long getServerTime() {
//        return Long.parseLong((String) call(new RedisClosure() {
//            @Override
//            public String execute(final Jedis jedis, final String appIds) {
//                return jedis.time().get(0);
//            }
//        }));
//    }
//
//
//    private Object doCallImpl(final RedisClosure redisClosure) throws Exception {
//        Jedis jedis = null;
//
//        try {
//            jedis = pool.getResource();
//
////            if (!jedis.isConnected() || !jedis.ping().equals("PONG")) {
////                throw new GAEException(ErrorType.PROGRAMMING, "Jedis resource isn't connected or doesn't answer ping.");
////            }
//            return redisClosure.execute(jedis, appId);
//        } finally {
//            if (jedis != null) {
//                jedis.close();
//            }
//        }
//    }

    public boolean tryToLock(final String key, final String locker, final int expirationSeconds) {
        try (final Jedis jedis = getPool().getResource()) {

            if ("ok".equalsIgnoreCase(jedis.set(key, locker, new SetParams().nx().ex(expirationSeconds)))) {
                // if lock adquired
                return true;
            }
        }
        return false;
    }

    public boolean releaseLock(final String key, final String locker) {

        final String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                "        return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "        return 0\n" +
                "end";

        try (final Jedis jedis = getPool().getResource()) {
            return 0 != (Long) jedis.eval(script, Collections.singletonList(key), Collections.singletonList(locker));
        }
    }
}
