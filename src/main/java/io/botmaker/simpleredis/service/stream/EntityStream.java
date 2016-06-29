package io.botmaker.simpleredis.service.stream;

import io.botmaker.simpleredis.dao.DAO;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import io.botmaker.simpleredis.util.ExceptionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EntityStream<E extends RedisEntity, D extends DAO<E>> implements AutoCloseable {

    private static final String SCRIPT =
            "local result = {}\n" +
                    "local scanResult = redis.call('SCAN', ARGV[1], 'MATCH', ARGV[2], 'COUNT', ARGV[3])\n" +
                    "result[1] = scanResult[1]\n" +
                    "result[2] = redis.call('MGET', unpack(scanResult[2]))\n" +
                    "return result\n";

    private final Jedis jedis;
    private final Report report = new Report();

    public EntityStream() {
        jedis = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getRedisServer().getPool().getResource();
    }

    public Stream<E> streamEntity(final String searchPrefix, final Class<D> daoClass, @Nullable final Predicate<? super String> keyFilter) {
        return streamEntity(searchPrefix, daoClass, keyFilter, 500);
    }

    public Stream<E> streamEntity(final String searchPrefix, final Class<D> daoClass, @Nullable final Predicate<? super String> keyFilter, final int redisScanCount) {
        final D dao = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getDAO(daoClass);
        return
                StreamSupport.stream(new Supplier(jedis.dbSize(), searchPrefix, redisScanCount), false).
                        parallel().
                        filter(key -> keyFilter == null || keyFilter.test(key)).
                        map(dataString -> {
                            try {
                                final E result = dao.buildPersistentObjectInstanceFromPersistedStringData(dataString);
                                report.addSuccessful();
                                return result;
                            } catch (final Exception e) {
                                report.addProblems("RedisStream problem on item with data [" + dataString + "]: " + e.getMessage() + " -> " + ExceptionUtils.exceptionToString(e));
                                return null;
                            }
                        }).
                        filter(i -> i != null);
    }

    public void close() {
        jedis.close();
    }

    public Report getReport() {
        if (report.endingTime == 0L) {
            report.endingTime = System.currentTimeMillis();
        }
        return report;
    }

    public final static class Report {

        private final StringBuilder problemsBuffer = new StringBuilder(1000);
        public int problemsCounter;
        public int successfulCounter;
        private long startingTime;
        private long endingTime;

        public boolean hasAnyProblems() {
            return problemsCounter > 0;
        }

        public synchronized void addSuccessful() {
            successfulCounter++;
        }

        public int getTotal() {
            return successfulCounter + problemsCounter;
        }

        public synchronized void addProblems(final String problem) {
            if (problemsBuffer.length() < 10000) {
                problemsBuffer.append(problem).append("\n\n");
            }
            problemsCounter++;
        }

        public String getExecutionReport() {
            final int total = getTotal();
            String itemsPerSec = "N/A";

            if (startingTime != 0L && endingTime != 0L) {
                itemsPerSec = Integer.toString((int) (total / (((endingTime - startingTime) / 1000f))));
            }

            return "[" + (problemsCounter == 0 ? "No " : problemsCounter) + "] problems found " +
                    "when processing [" + total + "] items. " +
                    "[" + itemsPerSec + "] items per sec. " +
                    "processing time was [" + getRunningTime() + "] secs\n" +
                    problemsBuffer;
        }

        public String getProblemsReport() {
            return problemsBuffer.toString();
        }

        public String getRunningTime() {
            if (startingTime == 0L || endingTime == 0L) {
                return "N/A";
            }
            return Integer.toString(((int) ((endingTime - startingTime) / 1000f)));
        }
    }

    public final class Supplier extends Spliterators.AbstractSpliterator<String> {

        private final List<String> page = new ArrayList<>(1000);
        private final String pattern;
        private final int redisScanCount;
        private boolean keepWorking = true;
        private String cursor = ScanParams.SCAN_POINTER_START;

        public Supplier(final long dbSize, final String searchPrefix, final int redisScanCount) {
            super(dbSize == 0 ? 1 : dbSize, Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.CONCURRENT);

            this.pattern = searchPrefix + "*";
            this.redisScanCount = redisScanCount;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super String> action) {
            if (page.isEmpty() && keepWorking) {
                if (report.startingTime == 0L) {
                    report.startingTime = System.currentTimeMillis();
                }
                do {
                    final List evalResult = (List) jedis.eval(SCRIPT, Collections.emptyList(), Arrays.asList(cursor, pattern, Integer.toString(redisScanCount)));
                    cursor = evalResult.get(0).toString();
                    keepWorking = !ScanParams.SCAN_POINTER_START.equals(cursor);
                    page.addAll(((List<String>) evalResult.get(1)));
                }
                while (page.isEmpty() && !ScanParams.SCAN_POINTER_START.equals(cursor));
            }

            if (page.isEmpty()) {
                return false;
            }
            action.accept(page.remove(0));
            return true;
        }
    }
}
