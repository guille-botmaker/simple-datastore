package com.zupcat.cache;

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
    protected int getCacheTimeoutSecs() {
        return 0;
    }

    @Override
    public Object get(final String key) {
        return null;
    }

    @Override
    public void remove(final String key) {
        // nothing to do
    }

    @Override
    public void put(final String key, final Object value) {
        // nothing to do
    }
}
