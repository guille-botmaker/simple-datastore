package io.botmaker.simpleredis.property;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;

public final class ByteArrayProperty extends PropertyMeta<byte[]> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public ByteArrayProperty(final RedisEntity owner) {
        super(owner);
    }

    protected byte[] getValueImpl(final DataObject dataObject) {
        if (!dataObject.has(name)) {
            return null;
        }

        final String s = dataObject.getString(name);

        return s == null ? null : Base64.decodeBase64(s);
    }

    @Override
    public void setFromStringValue(final String stringValue, final boolean forceAudit) {
        throw new UnsupportedOperationException("ByteArrayProperty does not implement this method");
    }

    protected void setValueImpl(final byte[] value, final DataObject dataObject) {
        if (value == null) {
            dataObject.remove(name);
        } else {
            dataObject.put(name, Base64.encodeBase64String(value));
        }
    }
}
