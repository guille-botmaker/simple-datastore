package com.zupcat.cache;

import javax.cache.CacheStatistics;
import java.util.Map;

public final class NoMemCache extends MemCache {

    private final static Object LOCK = new Object();

    private static NoMemCache INSTANCE;

    public static MemCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new NoMemCache();
                }
            }
        }
        return INSTANCE;
    }

    private NoMemCache() {
        // nothing to do
    }

    @Override
    protected void configure(final Map props) {
    }

    public Object get(final String key) {
        return null;
    }

    public boolean contains(final String key) {
        return false;
    }

    public void put(final String key, final Object value) {
        // nothing to do
    }

    public void remove(final String key) {
        // nothing to do
    }

    public CacheStatistics getStats() {
        return null;
    }

    public void invalidate() {
        // nothing to do
    }
}
