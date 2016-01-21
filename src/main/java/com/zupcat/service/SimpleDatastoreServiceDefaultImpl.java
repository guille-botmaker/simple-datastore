package com.zupcat.service;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.zupcat.dao.DAO;

import java.util.HashMap;
import java.util.Map;

public final class SimpleDatastoreServiceDefaultImpl implements SimpleDatastoreService {

    private final Map<Class, DAO> daoMap = new HashMap<>();
    private final Map<String, DAO> daoByEntityNameMap = new HashMap<>();
    private boolean loggingDatastoreCalls = false;
    private String datastoreServiceAccountEmail;
    private String datastorePrivateKeyP12FileLocation;

    @Override
    public void setDatastoreCallsLogging(final boolean activate) {
        loggingDatastoreCalls = activate;
    }

    @Override
    public boolean isDatastoreCallsLoggingActivated() {
        return loggingDatastoreCalls;
    }

    public void configProtoBuf(final String datastoreServiceAccountEmail, final String datastorePrivateKeyP12FileLocation) {
        this.datastoreServiceAccountEmail = datastoreServiceAccountEmail;
        this.datastorePrivateKeyP12FileLocation = datastorePrivateKeyP12FileLocation;

        fixProtobufConfigOnCurrentThread();
    }

    public void fixProtobufConfigOnCurrentThread() {
        final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        helper.setUp();
    }

    public boolean isProtoBufMode() {
        return this.datastoreServiceAccountEmail != null;
    }

    public String getDatastoreServiceAccountEmail() {
        return datastoreServiceAccountEmail;
    }

    public String getDatastorePrivateKeyP12FileLocation() {
        return datastorePrivateKeyP12FileLocation;
    }

    public String getDataSetId() {
        if (this.datastoreServiceAccountEmail == null) {
            return null;
        }

        String tmp = datastoreServiceAccountEmail.substring(datastoreServiceAccountEmail.indexOf("@") + 1);
        tmp = tmp.substring(0, tmp.indexOf("."));
        return tmp;
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
