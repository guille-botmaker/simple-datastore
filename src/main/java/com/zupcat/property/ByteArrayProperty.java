package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;
import java.nio.charset.Charset;

public class ByteArrayProperty extends PropertyMeta<byte[]> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public ByteArrayProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected byte[] getValueImpl(final ObjectVar objectVar) {
        final String result = objectVar.getString(name);

        return result == null ? null : result.getBytes(Charset.forName("UTF-8"));
    }

    protected void setValueImpl(final byte[] value, final ObjectVar objectVar) {
        objectVar.set(name, value == null ? null : new String(value, Charset.forName("UTF-8")));
    }
}
