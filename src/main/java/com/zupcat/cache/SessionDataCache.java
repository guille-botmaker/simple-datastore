package com.zupcat.cache;

public final class SessionDataCache extends MemCache {

    private final static Object LOCK = new Object();
    private final static int MINUTES_15 = 60 * 15;

    private static SessionDataCache INSTANCE;

    public static MemCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionDataCache();
                }
            }
        }
        return INSTANCE;
    }

    private SessionDataCache() {
        // nothing to do
    }

    @Override
    protected int getCacheTimeoutSecs() {
        return MINUTES_15;
    }
}
