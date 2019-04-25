package io.botmaker.simpleredis.service;

import io.botmaker.simpleredis.dao.DAO;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

/**
 * Entry point for service access
 */
public interface SimpleDatastoreService {

    void setDatastoreCallsLogging(final boolean activate);

    boolean isDatastoreCallsLoggingActivated();

    boolean isProductionEnvironment();

    void registerDAO(final DAO dao);

    <T extends DAO> T getDAO(final Class<T> daoClass);

    DAO getDAO(final String entityName);

    // Redis methods
    void configRedisServer(final String appId, final String redisHost, final int redisPost, final boolean isProductionEnvironment);

    void configRedisServer(final String appId, final String redisHost, final int redisPort, final boolean isProductionEnvironment, final String redisAuthPassword);

    void configureSentinel(final String masterName, final String appId, final String sentinelHost, final int sentinelPort, final boolean isProductionEnvironment, final String redisAuthPassword, final List<ImmutablePair<ImmutablePair<String, Integer>, ImmutablePair<String, Integer>>> addressTranslators);

    RedisServer getRedisServer();
}
