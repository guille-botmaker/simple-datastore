package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;

public final class ListIntegerProperty extends AbstractListAnyProperty<Integer> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public ListIntegerProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected void writeItem(final Encoder encoder, final Integer item) throws IOException {
        writeSafeInteger(encoder, item);
    }

    @Override
    protected Integer readItem(final Decoder decoder) throws IOException {
        return readSafeInteger(decoder);
    }
}
