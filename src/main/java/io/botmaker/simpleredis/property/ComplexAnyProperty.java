package io.botmaker.simpleredis.property;

import io.botmaker.simpleredis.dao.SerializationHelper;
import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.DatastoreEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;

public final class ComplexAnyProperty<T> extends PropertyMeta<T> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public ComplexAnyProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected T getValueImpl(final DataObject dataObject) {
        if (!dataObject.has(name)) {
            return null;
        }

        final String s = dataObject.getString(name);

        if (s == null) {
            return null;
        } else {
            return (T) SerializationHelper.getObjectFromBytes(Base64.decodeBase64(s), false);
        }
    }

    @Override
    public void setFromStringValue(final String stringValue, final boolean forceAudit) {
        throw new UnsupportedOperationException("ComplexAnyProperty does not implement this method");
    }

    @Override
    protected void setValueImpl(final T value, final DataObject dataObject) {
        if (value == null) {
            dataObject.remove(name);
        } else {
            dataObject.put(name, Base64.encodeBase64String(SerializationHelper.getBytes(value, false)));
        }
    }
}
