package io.botmaker.simpleredis.service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class RedisServer {

    private static final Logger LOGGER = Logger.getLogger(RedisServer.class.getName());

    private JedisPool pool;
    //    private final JedisSentinelPool pool;
    private String appId;

    public void configure(final String redisHost, final String appId) {
        this.appId = appId;

        final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(3);
        config.setTestWhileIdle(true);
        config.setTestOnCreate(false);

        pool = new JedisPool(config, redisHost, Protocol.DEFAULT_PORT, 10000);
//        pool = new JedisSentinelPool(MASTER_NAME, Collections.singleton(new HostAndPort(host, port).toString()), config, 2000);
    }

    public JedisPool getPool() {
        return pool;
    }

    public String getAppId() {
        return appId;
    }

    public T call(final RedisClosure redisClosure) {
//        if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
//            return null;
//        }
        final Object[] out = new Object[1];

        final RetryingExecutor executor = new RetryingExecutor(5, 200, new IClosure() {
            @Override
            public void execute(final Object params) throws Exception {
                out[0] = doCallImpl(redisClosure);
            }
        }, null);

        try {
            executor.startExecution();
        } catch (final Throwable throwable) {
            final String msg = "Problems accessing Redis Server: " + throwable.getMessage();
            LOGGER.log(Level.SEVERE, msg, throwable);
            ExceptionUtils.report(throwable);

            throw new RuntimeException(msg, throwable);
        }
        return (T) out[0];
    }

    public boolean tryToRunInsideSafeLock(final String lockKey, final int lockTimeoutInSeconds, final Runnable runnable) {

        if (!tryToGetLock(lockKey, lockTimeoutInSeconds)) {
            return false;
        }

        runnable.run();

        releaseLock(lockKey);
        return true;
    }

    private boolean tryToGetLock(final String key, final int lockTimeoutInSeconds) {
        // REFERENCE: http://redis.io/commands/setnx (distributed option: http://redis.io/topics/distlock)
        final long serverTime = getServerTime();
        final String timeout = String.valueOf(serverTime + lockTimeoutInSeconds);

        final Long returnValue = (Long) call(new RedisClosure() {
            @Override
            public Long execute(final Jedis jedis, final String appIds) {
                return jedis.setnx(appIds + key, timeout);
            }
        });

        // lock acquire if 1
        if (returnValue == 1) {
            return true;
        }

        String currentLockTimeoutString = ((RedisServer<String>) RedisServer.getInstance()).call(new RedisClosure() {
            @Override
            public String execute(final Jedis jedis, final String appIds) {
                return jedis.get(appIds + key);
            }
        });

        // lock is set if it is NOT timeouted
        if (currentLockTimeoutString != null && Long.parseLong(currentLockTimeoutString) > serverTime) {
            return false;
        }

        currentLockTimeoutString = ((RedisServer<String>) RedisServer.getInstance()).call(new RedisClosure() {
            @Override
            public String execute(final Jedis jedis, final String appIds) {
                return jedis.getSet(appIds + key, timeout);
            }
        });

        // lock acquire if the current lock is timeouted
        return currentLockTimeoutString != null && Long.parseLong(currentLockTimeoutString) <= serverTime;
    }

    private long getServerTime() {
        return Long.parseLong((String) call(new RedisClosure() {
            @Override
            public String execute(final Jedis jedis, final String appIds) {
                return jedis.time().get(0);
            }
        }));
    }

    private void releaseLock(final String key) {
        RedisServer.getInstance().call(new RedisClosure() {
            @Override
            public Object execute(final Jedis jedis, final String appIds) {
                jedis.del(appIds + key);
                return null;
            }
        });
    }

    private Object doCallImpl(final RedisClosure redisClosure) throws Exception {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();

//            if (!jedis.isConnected() || !jedis.ping().equals("PONG")) {
//                throw new GAEException(ErrorType.PROGRAMMING, "Jedis resource isn't connected or doesn't answer ping.");
//            }
            return redisClosure.execute(jedis, appId);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
