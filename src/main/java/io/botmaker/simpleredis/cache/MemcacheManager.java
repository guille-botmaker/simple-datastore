package io.botmaker.simpleredis.cache;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import io.botmaker.simpleredis.util.IClosure;
import io.botmaker.simpleredis.util.RetryingExecutor;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class acts as a MemCache proxy. Adds retrying functionality
 */
public final class MemcacheManager implements Serializable {

    public static final int ITEM_SIZE_LIMIT = 1024 * 1024 - 96; // took from https://code.google.com/p/googleappengine/source/browse/trunk/java/src/main/com/google/appengine/api/memcache/AsyncMemcacheServiceImpl.java
    private static final long serialVersionUID = 9497964351314234L;
    private static final Logger log = Logger.getLogger(MemcacheManager.class.getName());
    private static final Object LOCK_OBJECT = new Object();
    private static MemcacheManager instance;


    private MemcacheManager() {
        // nothing to do
    }

    public static MemcacheManager getInstance() {
        if (instance == null) {
            synchronized (LOCK_OBJECT) {
                if (instance == null) {
                    instance = new MemcacheManager();
                }
            }
        }
        return instance;
    }

    public Object get(final String key) {
        final Object[] count = new Object[1];
        count[0] = null;

        final RetryingExecutor executor = new RetryingExecutor(3, 500, new IClosure() {
            public void execute(final Object arg0) throws Exception {
                final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
                count[0] = memcache.get(key);
            }
        }, null);

        try {
            executor.startExecution();
        } catch (final Throwable e) {
            log.log(Level.WARNING, "Problems when getting object from MemCache. Key [" + key + "]: " + e.getMessage(), e);
        }
        return count[0];
    }

    public void put(final String key, final Object value, final Expiration expiration) {
        final RetryingExecutor executor = new RetryingExecutor(4, 500, new IClosure() {

            public void execute(final Object arg0) throws Exception {
                final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
                memcache.put(key, value, expiration, MemcacheService.SetPolicy.SET_ALWAYS);
            }
        }, null);

        try {
            executor.startExecution();
        } catch (final Throwable e) {
            log.log(Level.WARNING, "Problems when putting object to MemCache. Key [" + key + "]: " + e.getMessage(), e);
        }
    }
}
