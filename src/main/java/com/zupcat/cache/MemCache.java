package com.zupcat.cache;

import com.zupcat.service.SimpleDatastoreServiceFactory;

import javax.cache.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MemCache {

    private final static Object LOCK_OBJECT = new Object();

    protected static final Logger logger = Logger.getLogger(MemCache.class.getName());
    protected Cache cache;


    protected MemCache() {
        synchronized (LOCK_OBJECT) {
            try {
                final Map props = new HashMap();
                configure(props);

                if (!props.isEmpty()) {
                    final CacheFactory factory = CacheManager.getInstance().getCacheFactory();
                    cache = factory.createCache(props);
                }
            } catch (final CacheException ex) {
                logger.log(Level.SEVERE, "Error creating Memcache: " + ex.getMessage(), ex);
            }
        }
    }

    protected abstract void configure(final Map props);


    public Object get(final String key) {
        final boolean loggingActivated = SimpleDatastoreServiceFactory.getSimpleDatastoreService().isDatastoreCallsLoggingActivated();

        try {
            if (loggingActivated) {
                logger.log(Level.SEVERE, "PERF - MemCache.get", new Exception());
            }
            return cache.get(key);
        } catch (final Throwable e) {
            logger.log(Level.WARNING, "Problems when getting key [" + key + "] from MemCache: " + e.getMessage(), e);
            return null;
        }
    }

    public boolean contains(final String key) {
        final boolean loggingActivated = SimpleDatastoreServiceFactory.getSimpleDatastoreService().isDatastoreCallsLoggingActivated();

        try {
            if (loggingActivated) {
                logger.log(Level.SEVERE, "PERF - MemCache.contains", new Exception());
            }
            return cache.containsKey(key);
        } catch (final Throwable e) {
            logger.log(Level.WARNING, "Problems checking contains on MemCache. Key [" + key + "]: " + e.getMessage(), e);
            return false;
        }
    }

    public void put(final String key, final Object value) {
        final boolean loggingActivated = SimpleDatastoreServiceFactory.getSimpleDatastoreService().isDatastoreCallsLoggingActivated();

        try {
            if (loggingActivated) {
                logger.log(Level.SEVERE, "PERF - MemCache.put", new Exception());
            }
            synchronized (LOCK_OBJECT) {
                cache.put(key, value);
            }
        } catch (final Throwable e) {
            logger.log(Level.WARNING, "Problems when putting object to MemCache. Key [" + key + "]: " + e.getMessage(), e);
        }
    }

    public void remove(final String key) {
        final boolean loggingActivated = SimpleDatastoreServiceFactory.getSimpleDatastoreService().isDatastoreCallsLoggingActivated();

        try {
            if (loggingActivated) {
                logger.log(Level.SEVERE, "PERF - MemCache.remove", new Exception());
            }
            synchronized (LOCK_OBJECT) {
                cache.remove(key);
            }
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Problems when removing object from MemCache. Key [" + key + "]: " + e.getMessage(), e);
        }
    }

    public CacheStatistics getStats() {
        return cache.getCacheStatistics();
    }

    public void invalidate() {
        synchronized (LOCK_OBJECT) {
            cache.clear();
        }
    }
}
