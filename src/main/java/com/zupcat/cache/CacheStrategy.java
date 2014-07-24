package com.zupcat.cache;

import java.io.Serializable;

public enum CacheStrategy implements Serializable {

    NO_CACHE(new NoMemCache()), // NullObject pattern
    APPLICATION_CACHE(new MemCache(60 * 60 * 24)), // a day
    SESSION_CACHE(new MemCache(60 * 15)); // 15 minutes

    private final MemCache strategy;

    private CacheStrategy(final MemCache strategy) {
        this.strategy = strategy;
    }

    public MemCache get() {
        return strategy;
    }
}
