package io.botmaker.simpleredis.property;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.function.Function;

public class StringProperty extends PropertyMeta<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper mapper = new ObjectMapper(jsonFactory);

    public StringProperty(final RedisEntity owner) {
        super(owner);
    }

    protected String getValueImpl(final DataObject dataObject) {
        return dataObject.has(name) ? dataObject.getString(name) : null;
    }

    protected void setValueImpl(final String value, final DataObject dataObject) {
        dataObject.put(name, (value == null ? null : (options.stringIsOnlyLowerCase ? value.toLowerCase() : value)));
    }

    @Override
    public void setFromStringValue(final String stringValue, final boolean forceAudit) {
        set(stringValue, forceAudit);
    }

    public boolean hasData() {
        final String s = this.get();

        return s != null && s.trim().length() > 0;
    }

    public <T> T load(final Class<T> type, final Function<ObjectMapper, JavaType> typeFactory) {
        final String s = this.get();

        if (s != null) {
            try {
                return mapper.readValue(jsonFactory.createParser(new StringReader(s)), typeFactory.apply(mapper));
            } catch (final IOException e) {
                // nothing to do (exception could not happen)
            }
        }
        return null;
    }

    public void save(final Object o) {
        String s = null;

        if (o != null) {
            final StringWriter stringWriter = new StringWriter(500);
            try {
                mapper.writeValue(stringWriter, o);
            } catch (final IOException e) {
                // nothing to do (exception could not happen)
            }
            s = stringWriter.toString();
        }
        this.set(s);
    }
}
