package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

public final class DataObjectProperty<T extends DataObject> extends PropertyMeta<T> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public DataObjectProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected T getValueImpl(final DataObject dataObject) {
        return (T) dataObject.getJSONObject(name);
    }

    @Override
    protected void setValueImpl(final T value, final DataObject dataObject) {
        dataObject.put(name, value);
    }
}
