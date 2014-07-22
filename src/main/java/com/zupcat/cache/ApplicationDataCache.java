package com.zupcat.cache;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

import java.util.Map;

public final class ApplicationDataCache extends MemCache {

    private final static Object LOCK = new Object();

    private static ApplicationDataCache INSTANCE;

    public static MemCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new ApplicationDataCache();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void configure(final Map props) {
        props.put(MemcacheService.SetPolicy.SET_ALWAYS, true);
        props.put(GCacheFactory.EXPIRATION_DELTA, 86400); // a day
    }
}
