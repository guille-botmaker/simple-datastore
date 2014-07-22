package com.zupcat.service;

public interface SimpleDatastoreService {

    void setDatastoreCallsLogging(final boolean activate);

    boolean isDatastoreCallsLoggingActivated();
}
