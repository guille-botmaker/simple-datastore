package com.zupcat.cache;

public final class ApplicationDataCache extends MemCache {

    private final static Object LOCK = new Object();
    private final static int A_DAY = 60 * 60 * 24;

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

    private ApplicationDataCache() {
        // nothing to do
    }


    @Override
    protected int getCacheTimeoutSecs() {
        return A_DAY;
    }
}
