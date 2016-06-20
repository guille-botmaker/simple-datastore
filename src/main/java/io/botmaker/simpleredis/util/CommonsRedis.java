package io.botmaker.simpleredis.util;

import io.botmaker.simpleredis.service.RedisServer;
import redis.clients.jedis.Jedis;

public final class CommonsRedis {

    /**
     * @return currentLockValue or null
     */
    public static String tryToLock(final String lockKey, final String lockValue, final int expiringSeconds, final RedisServer redisServer) {
        final String script =
                "local value = redis.call('get', KEYS[1])\n" +
                        "if not value or value == '' or value == ARGV[1] or ARGV[1] == '' then\n" +
                        "redis.call('set', KEYS[1], ARGV[1])\n" +
                        "redis.call('expire', KEYS[1], ARGV[2])\n" +
                        "return ARGV[1]\n" +
                        "else\n" +
                        "return nil\n" +
                        "end";

        final String currentLockValue;
        try (final Jedis jedis = redisServer.getPool().getResource()) {
            currentLockValue = (String) jedis.eval(script, 1, lockKey, lockValue, "" + expiringSeconds);
        }
        return currentLockValue;
    }
}
