package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;
import org.json.JSONObject;

import java.io.Serializable;

public final class DataObjectProperty<T extends DataObject> extends PropertyMeta<T> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private final Class<T> itemClass;

    public DataObjectProperty(final DatastoreEntity owner, final Class<T> _itemClass) {
        super(owner);
        itemClass = _itemClass;
    }

    public Class<T> getItemClass() {
        return itemClass;
    }

    @Override
    protected T getValueImpl(final DataObject dataObject) {
        if (!dataObject.has(name)) {
            return null;
        }

        final JSONObject jsonObject = dataObject.getJSONObject(name);
        final T result;

        if (itemClass.isInstance(jsonObject)) {
            result = (T) jsonObject;
        } else {
            try {
                result = itemClass.newInstance();
            } catch (final Exception _exception) {
                throw new RuntimeException("Could not instantiate object of class [" + itemClass.getName() + "]. Maybe missing empty constructor?: " + _exception.getMessage(), _exception);
            }

            result.mergeWith(jsonObject);
            dataObject.put(name, result);
        }
        return result;
    }

    @Override
    protected void setValueImpl(final T value, final DataObject dataObject) {
        dataObject.put(name, value);
    }
}
