package com.zupcat.cache;

import java.io.Serializable;

public enum CacheStrategy implements Serializable {

    NO_CACHE(NoMemCache.getInstance()),
    APPLICATION_CACHE(ApplicationDataCache.getInstance()),
    SESSION_CACHE(SessionDataCache.getInstance());

    private final MemCache strategy;

    private CacheStrategy(final MemCache strategy) {
        this.strategy = strategy;
    }

    public MemCache get() {
        return strategy;
    }
}
