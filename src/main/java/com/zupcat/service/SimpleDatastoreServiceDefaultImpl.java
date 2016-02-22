package com.zupcat.service;

import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.zupcat.dao.DAO;

import java.io.IOException;
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

    public void configRemoteDatastore(final String remoteAppId, final String datastoreServiceAccountEmail, final String datastorePrivateKeyP12FileLocation, final boolean useLocalDevServer) {
        final RemoteApiOptions options = new RemoteApiOptions();

        if (useLocalDevServer) {
            options
                    .server("localhost", 8080)
                    .useDevelopmentServerCredential();
        } else {
            options
                    .server(remoteAppId + ".appspot.com", 443)
                    .useServiceAccountCredential(datastoreServiceAccountEmail, datastorePrivateKeyP12FileLocation);
        }

        try {
            new RemoteApiInstaller().install(options);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
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
