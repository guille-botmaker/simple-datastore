package com.zupcat.service;

import com.zupcat.dao.DAO;

import java.util.HashMap;
import java.util.Map;

public final class SimpleDatastoreServiceDefaultImpl implements SimpleDatastoreService {

    private final Map<Class, DAO> daoMap = new HashMap<>();
    private final Map<String, DAO> daoByEntityNameMap = new HashMap<>();
    private boolean loggingDatastoreCalls = false;

    @Override
    public void setDatastoreCallsLogging(final boolean activate) {
        loggingDatastoreCalls = activate;
    }

    @Override
    public boolean isDatastoreCallsLoggingActivated() {
        return loggingDatastoreCalls;
    }

    @Override
    public void registerDAO(final DAO dao) {
        daoMap.put(dao.getClass(), dao);
        daoByEntityNameMap.put(dao.getEntityName(), dao);
    }

    @Override
    public <T> T getDAO(final Class<T> daoClass) {
        return (T) daoMap.get(daoClass);
    }

    @Override
    public DAO getDAO(final String entityName) {
        return daoByEntityNameMap.get(entityName);
    }
}
