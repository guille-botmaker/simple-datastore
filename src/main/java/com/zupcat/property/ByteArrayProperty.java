package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.config.PropertyMeta;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;

public class ByteArrayProperty extends PropertyMeta<byte[]> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private transient byte[] cache;

    public ByteArrayProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected byte[] getValueImpl(final ObjectVar objectVar) {
        if (cache == null) {
            final String result = objectVar.getString(name);

            cache = result == null ? null : Base64.decodeBase64(result);
        }
        return cache;
    }

    protected void setValueImpl(final byte[] value, final ObjectVar objectVar) {
        cache = value;
    }

    @Override
    public void commit() {
        super.commit();

        final byte[] value = get();

        final ObjectVar objectVar = getOwner().getInternalObjectHolder().getObjectVar();

        objectVar.set(name, value == null ? null : Base64.encodeBase64String(value));
    }
}
