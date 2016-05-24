package io.botmaker.simpleredis.service;

import io.botmaker.simpleredis.dao.DAO;
import io.botmaker.simpleredis.dao.ResourceDAO;

import java.util.HashMap;
import java.util.Map;

public final class SimpleDatastoreServiceDefaultImpl implements SimpleDatastoreService {

    private final Map<Class, DAO> daoMap = new HashMap<>();
    private final Map<String, DAO> daoByEntityNameMap = new HashMap<>();
    private final RedisServer redisServer = new RedisServer();
    private boolean loggingDatastoreCalls = false;

    public SimpleDatastoreServiceDefaultImpl() {
        registerDAO(new ResourceDAO());
    }

    @Override
    public void setDatastoreCallsLogging(final boolean activate) {
        loggingDatastoreCalls = activate;
    }

    @Override
    public boolean isDatastoreCallsLoggingActivated() {
        return loggingDatastoreCalls;
    }

    @Override
    public void configRedisServer(final String appId, final String redisHost) {
        configRedisServer(appId, redisHost, null);
    }

    @Override
    public void configRedisServer(final String appId, final String redisHost, final String redisAuthPassword) {
        this.redisServer.configure(redisHost, appId, redisAuthPassword);
    }

    @Override
    public RedisServer getRedisServer() {
        return redisServer;
    }

    @Override
    public void registerDAO(final DAO dao) {
        daoMap.put(dao.getClass(), dao);
        daoByEntityNameMap.put(dao.getEntityName(), dao);
    }

    @Override
    public <T extends DAO> T getDAO(final Class<T> daoClass) {
        return (T) daoMap.get(daoClass);
    }

    @Override
    public DAO getDAO(final String entityName) {
        return daoByEntityNameMap.get(entityName);
    }
}
