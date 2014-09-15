package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public final class MapStringMapStringIntegerProperty extends AbstractMapStringAnyProperty<String, Map<String, Integer>> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    public MapStringMapStringIntegerProperty(final DatastoreEntity owner) {
        super(owner);
    }

    @Override
    protected void writeKeyValue(final Encoder encoder, final Entry<String, Map<String, Integer>> entry) throws IOException {
        // Key
        writeSafeString(encoder, entry.getKey());

        // Value
        final Map<String, Integer> itemsMap = entry.getValue();

        if (!itemsMap.isEmpty()) {
            encoder.writeMapStart();
            encoder.setItemCount(itemsMap.size());

            for (final Entry<String, Integer> itemEntry : itemsMap.entrySet()) {
                encoder.startItem();
                writeSafeString(encoder, itemEntry.getKey());
                writeSafeInteger(encoder, itemEntry.getValue());
            }

            encoder.writeMapEnd();
        }
    }

    @Override
    protected Entry<String, Map<String, Integer>> readKeyValue(final Decoder decoder) throws IOException {
        final Map<String, Integer> items = new HashMap<>();

        final Entry<String, Map<String, Integer>> result = new AbstractMap.SimpleEntry<>(readSafeString(decoder), items);

        for (long i = decoder.readMapStart(); i != 0; i = decoder.mapNext()) {
            for (long j = 0; j < i; j++) {
                final String key = readSafeString(decoder);
                items.put(key, readSafeInteger(decoder));
            }
        }
        return result;
    }
}
