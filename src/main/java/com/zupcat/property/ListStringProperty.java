package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;

public final class ListStringProperty extends AbstractListAnyProperty<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public ListStringProperty(final DatastoreEntity owner, final boolean sentToClient, final boolean auditable) {
        super(owner, sentToClient, auditable);
    }

    @Override
    protected void writeItem(final Encoder encoder, final String item) throws IOException {
        encoder.writeString(item);
    }

    @Override
    protected String readItem(final Decoder decoder) throws IOException {
        return decoder.readString();
    }
}
