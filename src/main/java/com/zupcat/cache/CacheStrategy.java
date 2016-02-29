package com.zupcat.cache;

import java.io.Serializable;

/**
 * Cache strategies for hide expiration strategies
 */
public enum CacheStrategy implements Serializable {

    NO_CACHE(new NoMemCache()), // NullObject pattern
    REQUEST_CACHE(new MemCache(60 * 2)), // 2 minutes
    APPLICATION_CACHE(new MemCache(60 * 60 * 24)), // a day
    SESSION_CACHE(new MemCache(60 * 15)); // 15 minutes

    private static final long serialVersionUID = 471847964351314234L;

    private final MemCache strategy;

    CacheStrategy(final MemCache strategy) {
        this.strategy = strategy;
    }

    public MemCache get() {
        return strategy;
    }
}
