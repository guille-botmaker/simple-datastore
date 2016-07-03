package io.botmaker.simpleredis.property;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectProperty<T> extends PropertyMeta<T> implements Serializable {

    private static final long serialVersionUID = 6181606486836703324L;
    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper mapper = new ObjectMapper(jsonFactory);

    private final Class<T> itemClass;

    public ObjectProperty(final RedisEntity owner, final Class<T> _itemClass) {
        super(owner);
        itemClass = _itemClass;
    }

    public Class<T> getItemClass() {
        return itemClass;
    }

    @Override
    protected T getValueImpl(final DataObject dataObject) {
        final String string = dataObject.optString(name, null);
        if (string == null) {
            return null;
        }

        try {
            return mapper.readValue(jsonFactory.createParser(new StringReader(string)), mapper.constructType(itemClass));
        } catch (final IOException e) {
            Logger.getLogger(ObjectProperty.class.getName()).log(Level.SEVERE, "dataObject [" + dataObject + "]: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setFromStringValue(final String stringValue, final boolean forceAudit) {
        throw new UnsupportedOperationException("ObjectProperty: setFromStringValue method is not implemented");
    }

    @Override
    protected void setValueImpl(final T value, final DataObject dataObject) {
        String s = null;

        if (value != null) {
            final StringWriter stringWriter = new StringWriter(500);
            try {
                mapper.writeValue(stringWriter, itemClass);
            } catch (final IOException e) {
                Logger.getLogger(ObjectProperty.class.getName()).log(Level.SEVERE, "dataObject [" + dataObject + "]: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
            s = stringWriter.toString();
        }
        dataObject.put(name, s);
    }
}
