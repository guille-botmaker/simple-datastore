package io.botmaker.simpleredis.cache;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import io.botmaker.simpleredis.service.SimpleDatastoreServiceFactory;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a wrapper for MemCache operations so different behaviors.
 * Also has the ability to log cache calls and stacktraces to find performance problems
 */
public class MemCache implements Serializable {

    private static final long serialVersionUID = 471847964351314234L;
    private static final Logger logger = Logger.getLogger(MemCache.class.getName());

    private final int cacheTimeoutSecs;

    protected MemCache(final int cacheTimeoutSecs) {
        this.cacheTimeoutSecs = cacheTimeoutSecs;
    }

    public Object get(final String key) {
        final boolean loggingActivated = SimpleDatastoreServiceFactory.getSimpleDatastoreService().isDatastoreCallsLoggingActivated();

        try {
            if (loggingActivated) {
                logger.log(Level.SEVERE, "PERF - MemCache.get", new Exception());
            }
            return MemcacheServiceFactory.getMemcacheService().get(key);
        } catch (final Throwable e) {
            logger.log(Level.WARNING, "Problems when getting key [" + key + "] from MemCache: " + e.getMessage(), e);
            return null;
        }
    }

    public void remove(final String key) {
        final boolean loggingActivated = SimpleDatastoreServiceFactory.getSimpleDatastoreService().isDatastoreCallsLoggingActivated();

        try {
            if (loggingActivated) {
                logger.log(Level.SEVERE, "PERF - MemCache.remove", new Exception());
            }
            MemcacheServiceFactory.getAsyncMemcacheService().delete(key);
        } catch (final Throwable e) {
            logger.log(Level.WARNING, "Problems when putting object to MemCache. Key [" + key + "]: " + e.getMessage(), e);
        }
    }

    public void put(final String key, final Object value) {
        final boolean loggingActivated = SimpleDatastoreServiceFactory.getSimpleDatastoreService().isDatastoreCallsLoggingActivated();

        try {
            if (loggingActivated) {
                logger.log(Level.SEVERE, "PERF - MemCache.put", new Exception());
            }
            MemcacheServiceFactory.getAsyncMemcacheService().put(key, value, Expiration.byDeltaSeconds(cacheTimeoutSecs), MemcacheService.SetPolicy.SET_ALWAYS);
        } catch (final Throwable e) {
            logger.log(Level.WARNING, "Problems when putting object to MemCache. Key [" + key + "]: " + e.getMessage(), e);
        }
    }
}
