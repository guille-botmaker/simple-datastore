package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

/**
 * Work as Map<String, Object>
 */
public final class JSONObjectProperty<T extends DataObject> extends PropertyMeta<T> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;


    public JSONObjectProperty(final DatastoreEntity owner) {
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
