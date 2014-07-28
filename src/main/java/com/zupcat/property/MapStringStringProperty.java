package com.zupcat.property;

import com.zupcat.model.PersistentObject;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;

public final class MapStringStringProperty extends AbstractMapStringAnyProperty<String, String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapStringStringProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, sentToClient, auditable);
    }

    @Override
    protected void writeKeyValue(final Encoder encoder, final Entry<String, String> entry) throws IOException {
        encoder.writeString(entry.getKey());
        encoder.writeString(entry.getValue());
    }

    @Override
    protected Entry<String, String> readKeyValue(final Decoder decoder) throws IOException {
        final String key = decoder.readString();
        return new AbstractMap.SimpleEntry<>(key, decoder.readString());
    }
}
