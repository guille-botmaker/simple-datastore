package io.botmaker.simpleredis.service;

import io.botmaker.simpleredis.dao.DAO;
import io.botmaker.simpleredis.dao.ResourceDAO;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SimpleDatastoreServiceDefaultImpl implements SimpleDatastoreService {

    private final Map<Class, DAO> daoMap = new HashMap<>();
    private final Map<String, DAO> daoByEntityNameMap = new HashMap<>();
    private final RedisServer redisServer = new RedisServer();
    private boolean loggingDatastoreCalls = false;
    private boolean isProductionEnvironment = false;

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
    public boolean isProductionEnvironment() {
        return isProductionEnvironment;
    }

    @Override
    public void configRedisServer(final String appId, final String redisHost, final int redisPort, final boolean isProductionEnvironment) {
        this.configRedisServer(appId, redisHost, redisPort, isProductionEnvironment, null);
    }

    @Override
    public void configRedisServer(final String appId, final String redisHost, final int redisPort, final boolean isProductionEnvironment, final String redisAuthPassword) {
        this.redisServer.configure(redisHost, redisPort, appId, redisAuthPassword);
        this.isProductionEnvironment = isProductionEnvironment;
    }

    @Override
    public void configureSentinel(final String masterName, final String appId, final String sentinelHost, final int sentinelPort, final boolean isProductionEnvironment,
                                  final String redisAuthPassword, final List<ImmutablePair<ImmutablePair<String, Integer>, ImmutablePair<String, Integer>>> addressTranslators) {
        this.redisServer.configureSentinel(sentinelHost, sentinelPort, masterName, appId, redisAuthPassword, addressTranslators);
        this.isProductionEnvironment = isProductionEnvironment;
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
