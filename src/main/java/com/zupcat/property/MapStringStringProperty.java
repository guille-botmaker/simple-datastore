package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;

public final class MapStringStringProperty extends AbstractMapStringAnyProperty<String, String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapStringStringProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected void writeKeyValue(final Encoder encoder, final Entry<String, String> entry) throws IOException {
        writeSafeString(encoder, entry.getKey());
        writeSafeString(encoder, entry.getValue());
    }

    @Override
    protected Entry<String, String> readKeyValue(final Decoder decoder) throws IOException {
        final String key = readSafeString(decoder);
        return new AbstractMap.SimpleEntry<>(key, readSafeString(decoder));
    }
}
