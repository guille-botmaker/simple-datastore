package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;

public final class MapIntegerIntegerProperty extends AbstractMapStringAnyProperty<Integer, Integer> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapIntegerIntegerProperty(final DatastoreEntity owner, final boolean sentToClient, final boolean auditable) {
        super(owner, sentToClient, auditable);
    }

    @Override
    protected void writeKeyValue(final Encoder encoder, final Entry<Integer, Integer> entry) throws IOException {
        encoder.writeInt(entry.getKey());
        encoder.writeInt(entry.getValue());
    }

    @Override
    protected Entry<Integer, Integer> readKeyValue(final Decoder decoder) throws IOException {
        final Integer key = decoder.readInt();
        return new AbstractMap.SimpleEntry<>(key, decoder.readInt());
    }
}
