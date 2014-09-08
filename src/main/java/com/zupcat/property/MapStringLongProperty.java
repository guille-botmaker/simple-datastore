package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;

public final class MapStringLongProperty extends AbstractMapStringAnyProperty<String, Long> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapStringLongProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected void writeKeyValue(final Encoder encoder, final Entry<String, Long> entry) throws IOException {
        encoder.writeString(entry.getKey());
        encoder.writeLong(entry.getValue());
    }

    @Override
    protected Entry<String, Long> readKeyValue(final Decoder decoder) throws IOException {
        final String key = decoder.readString();
        return new AbstractMap.SimpleEntry<>(key, decoder.readLong());
    }
}
