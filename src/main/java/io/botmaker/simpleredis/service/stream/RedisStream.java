package io.botmaker.simpleredis.service.stream;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class RedisStream extends AbstractStream {

    private static final String SCRIPT =
            "return redis.call('SCAN', ARGV[1], 'MATCH', ARGV[2], 'COUNT', ARGV[3])";

    public Stream<String> stream(final String searchPrefix, @Nullable final Predicate<? super String> keyFilter) {
        return stream(searchPrefix, keyFilter, 500);
    }

    public Stream<String> stream(final String searchPrefix, @Nullable final Predicate<? super String> keyFilter, final int redisScanCount) {
        return
                StreamSupport.stream(new Supplier(jedis.dbSize(), searchPrefix, redisScanCount, tenMinutesBatchProcess, jedis, SCRIPT), false).
                        parallel().
                        filter(key -> keyFilter == null || keyFilter.test(key));
    }
}
