package io.botmaker.simpleredis.model;

import io.botmaker.simpleredis.dao.SerializationHelper;
import io.botmaker.simpleredis.model.config.BYTE_ARRAY;
import io.botmaker.simpleredis.model.config.STRING;
import io.botmaker.simpleredis.property.ByteArrayProperty;
import io.botmaker.simpleredis.property.StringProperty;

/**
 * Usefull class for app parameters persistence.
 */
public final class Resource extends RedisEntity {

    private static final long serialVersionUID = -2259862423311176614L;

    public StringProperty TYPE;
    public ByteArrayProperty RAW;

    public Resource() {
        super(false, EXPIRING_NEVER);
    }

    public Resource(final boolean usesAppIdPrefix, final int secondsToExpire) {
        super(usesAppIdPrefix, secondsToExpire);
    }

    @Override
    protected void config() {
        TYPE = new STRING(this).sendToClient().build();
        RAW = new BYTE_ARRAY(this).sendToClient().build();
    }

    public Object getJavaObject() {
        final byte[] bytes = RAW.get();
        return bytes == null ? null : SerializationHelper.getObjectFromBytes(bytes, true);
    }

    public void setJavaObject(final Object javaObject) {
        RAW.set(javaObject == null ? null : SerializationHelper.getBytes(javaObject, true));
    }
}
