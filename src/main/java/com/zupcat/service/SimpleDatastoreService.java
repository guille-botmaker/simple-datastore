package com.zupcat.service;

import com.zupcat.dao.DAO;

/**
 * Entry point for service access
 */
public interface SimpleDatastoreService {

    void setDatastoreCallsLogging(final boolean activate);

    boolean isDatastoreCallsLoggingActivated();

    void registerDAO(final DAO dao);

    <T> T getDAO(final Class<T> daoClass);

    DAO getDAO(final String entityName);

    // protobuf methods
    void configProtoBuf(final String datastoreServiceAccountEmail, final String datastorePrivateKeyP12FileLocation, final String dataSetId);

    boolean isProtoBufMode();

    String getDatastoreServiceAccountEmail();

    String getDatastorePrivateKeyP12FileLocation();

    String getDataSetId();
}
