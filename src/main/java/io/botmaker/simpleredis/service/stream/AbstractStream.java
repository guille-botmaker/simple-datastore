package io.botmaker.simpleredis.service.stream;

import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;
import redis.clients.jedis.Jedis;

public abstract class AbstractStream implements AutoCloseable {

    protected final Jedis jedis;
    protected final Report report = new Report();

    public AbstractStream() {
        jedis = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getRedisServer().getPool().getResource();
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
}
