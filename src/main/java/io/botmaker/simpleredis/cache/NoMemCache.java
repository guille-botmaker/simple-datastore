package io.botmaker.simpleredis.cache;

/**
 * This implementation avoids MemCache usage
 */
public final class NoMemCache extends MemCache {

    protected NoMemCache() {
        super(0);
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
