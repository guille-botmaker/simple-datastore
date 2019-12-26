package io.botmaker.simpleredis.property;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import org.json.JSONObject;

import java.io.Serializable;

public class DataObjectProperty<T extends DataObject> extends PropertyMeta<T> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private final Class<T> itemClass;

    public DataObjectProperty(final RedisEntity owner, final Class<T> _itemClass) {
        super(owner);
        itemClass = _itemClass;
    }

    public Class<T> getItemClass() {
        return itemClass;
    }

    @Override
    protected T getValueImpl(final DataObject dataObject) {
        // NOTE: in multithread evironment, previously checking with "dataObject.has(name)" can get problems
        // so use opt instead
        JSONObject jsonObject = dataObject.optJSONObject(name);
        if (jsonObject == null || jsonObject == JSONObject.NULL) {
            jsonObject = new JSONObject();
            dataObject.put(name, jsonObject);
        }

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
    public void setFromStringValue(final String stringValue, final boolean forceAudit) {
        if (stringValue == null || stringValue.trim().length() == 0) {
            set(null, forceAudit);
        } else {
            try {
                final T result = itemClass.newInstance();
                result.mergeWith(new DataObject(stringValue));
                set(result, forceAudit);
            } catch (final Exception _exception) {
                throw new RuntimeException("Could not instantiate object of class [" + itemClass.getName() + "]. Maybe missing empty constructor?: " + _exception.getMessage(), _exception);
            }
        }
    }

    @Override
    protected void setValueImpl(final T value, final DataObject dataObject) {
        dataObject.put(name, value);
    }
}
