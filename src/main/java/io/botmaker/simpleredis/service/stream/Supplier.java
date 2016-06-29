package io.botmaker.simpleredis.service.stream;

import io.botmaker.simpleredis.model.TenMinutesBatchProcess;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;

import java.util.*;
import java.util.function.Consumer;

public final class Supplier extends Spliterators.AbstractSpliterator<String> {

    private final List<String> page = new ArrayList<>(1000);
    private final String pattern;
    private final int redisScanCount;
    private final TenMinutesBatchProcess tenMinutesBatchProcess;
    private final Jedis jedis;
    private final String script;
    private boolean keepWorking = true;
    private String cursor = ScanParams.SCAN_POINTER_START;

    public Supplier(final long dbSize, final String searchPrefix, final int redisScanCount, final TenMinutesBatchProcess tenMinutesBatchProcess,
                    final Jedis jedis, final String script) {
        super(dbSize == 0 ? 1 : dbSize, Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.CONCURRENT);

        this.tenMinutesBatchProcess = tenMinutesBatchProcess;
        this.script = script;
        this.jedis = jedis;
        this.pattern = searchPrefix + "*";
        this.redisScanCount = redisScanCount;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super String> action) {
        if (page.isEmpty() && keepWorking) {
            if (tenMinutesBatchProcess.STARTING_TIME.get() == 0L) {
                tenMinutesBatchProcess.STARTING_TIME.set(System.currentTimeMillis());
            }
            do {
                final List evalResult = (List) jedis.eval(script, Collections.emptyList(), Arrays.asList(cursor, pattern, Integer.toString(redisScanCount)));
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
