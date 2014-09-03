package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;

public class ByteArrayProperty extends PropertyMeta<byte[]> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public ByteArrayProperty(final DatastoreEntity owner, final byte[] initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable) {
        super(owner, initialValue, sentToClient, auditable, indexable);
    }

    protected byte[] getValueImpl(final ObjectVar objectVar) {
        final String result = objectVar.getString(name);

        return result == null ? null : result.getBytes();
    }

    protected void setValueImpl(final byte[] value, final ObjectVar objectVar) {
        objectVar.set(name, value == null ? null : new String(value));
    }
}
