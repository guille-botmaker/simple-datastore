package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public final class MapStringMapStringStringProperty extends AbstractMapStringAnyProperty<String, Map<String, String>> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapStringMapStringStringProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected void writeKeyValue(final Encoder encoder, final Entry<String, Map<String, String>> entry) throws IOException {
        // Key
        writeSafeString(encoder, entry.getKey());

        // Value
        final Map<String, String> itemsMap = entry.getValue();

        if (!itemsMap.isEmpty()) {
            encoder.writeMapStart();
            encoder.setItemCount(itemsMap.size());

            for (final Entry<String, String> itemEntry : itemsMap.entrySet()) {
                encoder.startItem();
                writeSafeString(encoder, itemEntry.getKey());
                writeSafeString(encoder, itemEntry.getValue());
            }

            encoder.writeMapEnd();
        }
    }

    @Override
    protected Entry<String, Map<String, String>> readKeyValue(final Decoder decoder) throws IOException {
        final Map<String, String> items = new HashMap<>();

        final Entry<String, Map<String, String>> result = new AbstractMap.SimpleEntry<>(readSafeString(decoder), items);

        for (long i = decoder.readMapStart(); i != 0; i = decoder.mapNext()) {
            for (long j = 0; j < i; j++) {
                final String key = readSafeString(decoder);
                items.put(key, readSafeString(decoder));
            }
        }
        return result;
    }
}
