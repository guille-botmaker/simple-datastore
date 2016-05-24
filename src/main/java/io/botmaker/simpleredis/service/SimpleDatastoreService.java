package io.botmaker.simpleredis.service;

import io.botmaker.simpleredis.dao.DAO;

/**
 * Entry point for service access
 */
public interface SimpleDatastoreService {

    void setDatastoreCallsLogging(final boolean activate);

    boolean isDatastoreCallsLoggingActivated();

    void registerDAO(final DAO dao);

    <T extends DAO> T getDAO(final Class<T> daoClass);

    DAO getDAO(final String entityName);

    // Redis methods
    void configRedisServer(final String appId, final String redisHost);

    void configRedisServer(final String appId, final String redisHost, final String redisAuthPassword);

    RedisServer getRedisServer();
}
