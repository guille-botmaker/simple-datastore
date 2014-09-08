package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

public class BooleanProperty extends PropertyMeta<Boolean> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public BooleanProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected Boolean getValueImpl(final ObjectVar objectVar) {
        return objectVar.getBoolean(name);
    }

    protected void setValueImpl(final Boolean value, final ObjectVar objectVar) {
        objectVar.set(name, value);
    }
}
