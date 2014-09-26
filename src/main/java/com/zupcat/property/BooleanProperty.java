package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

public final class BooleanProperty extends PropertyMeta<Boolean> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public BooleanProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected Boolean getValueImpl(final DataObject dataObject) {
        return dataObject.getBoolean(name);
    }

    protected void setValueImpl(final Boolean value, final DataObject dataObject) {
        dataObject.put(name, value);
    }
}
