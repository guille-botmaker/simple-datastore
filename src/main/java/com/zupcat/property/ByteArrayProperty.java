package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;

public final class ByteArrayProperty extends PropertyMeta<byte[]> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public ByteArrayProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected byte[] getValueImpl(final DataObject dataObject) {
        final String s = dataObject.getString(name);

        return s == null ? null : Base64.decodeBase64(s);
    }

    protected void setValueImpl(final byte[] value, final DataObject dataObject) {
        if (value == null) {
            dataObject.remove(name);
        } else {
            dataObject.put(name, Base64.encodeBase64String(value));
        }
    }
}
