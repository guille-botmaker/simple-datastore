package io.botmaker.simpleredis.service.stream;

import io.botmaker.simpleredis.dao.DAO;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import io.botmaker.simpleredis.util.ExceptionUtils;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class EntityStream<E extends RedisEntity, D extends DAO<E>> extends AbstractStream {

    private static final String SCRIPT =
            "local result = {}\n" +
                    "local scanResult = redis.call('SCAN', ARGV[1], 'MATCH', ARGV[2], 'COUNT', ARGV[3])\n" +
                    "result[1] = scanResult[1]\n" +
                    "result[2] = redis.call('MGET', unpack(scanResult[2]))\n" +
                    "return result\n";

    public Stream<E> stream(final String searchPrefix, final Class<D> daoClass, @Nullable final Predicate<? super String> keyFilter) {
        return stream(searchPrefix, daoClass, keyFilter, 500);
    }

    public Stream<E> stream(final String searchPrefix, final Class<D> daoClass, @Nullable final Predicate<? super String> keyFilter, final int redisScanCount) {
        final D dao = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getDAO(daoClass);
        return
                StreamSupport.stream(new Supplier(jedis.dbSize(), searchPrefix, redisScanCount, tenMinutesBatchProcess, jedis, SCRIPT), false).
                        parallel().
                        filter(key -> keyFilter == null || keyFilter.test(key)).
                        map(dataString -> {
                            try {
                                final E result = dao.buildPersistentObjectInstanceFromPersistedStringData(dataString);
                                tenMinutesBatchProcess.addSuccessful();
                                return result;
                            } catch (final Exception e) {
                                tenMinutesBatchProcess.addProblems("RedisStream problem on item with data [" + dataString + "]: " + e.getMessage() + " -> " + ExceptionUtils.exceptionToString(e));
                                return null;
                            }
                        }).
                        filter(i -> i != null);
    }
}
