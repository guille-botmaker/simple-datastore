package io.botmaker.simpleredis.property;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ObjectProperty<T> extends PropertyMeta<T> implements Serializable {

    private static final long serialVersionUID = 6181606486836703324L;
    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper mapper = new ObjectMapper(jsonFactory);

    private final Class<T> itemClass;
    private final boolean compress;
    private final Function<ObjectMapper, TypeReference> typeReference;

    public ObjectProperty(final RedisEntity owner, final Class<T> _itemClass, final Function<ObjectMapper, TypeReference> typeReference, final boolean compress) {
        super(owner);
        itemClass = _itemClass;
        this.typeReference = typeReference;
        this.compress = compress;
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
            final boolean compressed = string.charAt(0) == 'C';

            if (compressed) {
                return mapper.readValue(jsonFactory.createParser(new GZIPInputStream(new ByteArrayInputStream(Base64.decodeBase64(string.substring(1))))),
                        typeReference.apply(mapper));
            } else {
                return mapper.readValue(jsonFactory.createParser(new StringReader(string.substring(1))), typeReference.apply(mapper));
            }
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
            try {
                final ByteArrayOutputStream byteOutputStream = compress ? new ByteArrayOutputStream(20000) : null;
                final StringWriter stringWriter = compress ? null : new StringWriter(500);
                final OutputStream outputStream = compress ? new GZIPOutputStream(byteOutputStream, 50000) : null;

                try {
                    if (compress) {
                        mapper.writeValue(outputStream, value);
                        outputStream.close();
                        s = "C" + Base64.encodeBase64String(byteOutputStream.toByteArray());
                    } else {
                        mapper.writeValue(stringWriter, value);
                        stringWriter.close();
                        s = "R" + stringWriter.toString();
                    }
                } catch (final IOException e) {
                    Logger.getLogger(ObjectProperty.class.getName()).log(Level.SEVERE, "dataObject [" + dataObject + "]: " + e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            } catch (final IOException e) {
                throw new RuntimeException("Could not setValue [" + value + "]: " + e.getMessage(), e);
            }
        }
        dataObject.put(name, s);
    }
}
