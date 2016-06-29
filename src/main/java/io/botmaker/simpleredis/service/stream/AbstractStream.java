package io.botmaker.simpleredis.service.stream;

import io.botmaker.simpleredis.model.TenMinutesBatchProcess;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import redis.clients.jedis.Jedis;

public abstract class AbstractStream implements AutoCloseable {

    protected final Jedis jedis;
    protected final TenMinutesBatchProcess tenMinutesBatchProcess = new TenMinutesBatchProcess();

    public AbstractStream() {
        jedis = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getRedisServer().getPool().getResource();
    }

    public void close() {
        jedis.close();
    }

    public TenMinutesBatchProcess getTenMinutesBatchProcess() {
        if (tenMinutesBatchProcess.ENDING_TIME.get() == 0L) {
            tenMinutesBatchProcess.markProcessFinished();
        }
        return tenMinutesBatchProcess;
    }
}
