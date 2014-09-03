package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import org.apache.avro.io.*;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public abstract class AbstractMapStringAnyProperty<K, V> extends StringProperty implements Serializable, Map<K, V> {

    private static final long serialVersionUID = 6181606486836703354L;

    private static final BinaryEncoder reusableBinaryEncoder = EncoderFactory.get().binaryEncoder(new ByteArrayOutputStream(), null);
    private static final BinaryDecoder reusableBinaryDecoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(new byte[1]), null);

    // to avoid some extra parsing to String
    private transient Map<K, V> cache;


    public AbstractMapStringAnyProperty(final DatastoreEntity owner, final boolean sentToClient, final boolean auditable) {
        super(owner, null, sentToClient, auditable, false, false);
    }

    @Override
    protected void setValueImpl(final String value, final ObjectVar objectVar) {
        objectVar.set(name, value);
    }


    @Override
    public void commit() {
        super.commit();

        final Map<K, V> map = getMap();

        if (map.isEmpty()) {
            set(null);
        } else {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(500);
            final Encoder encoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, reusableBinaryEncoder);

            try {
                encoder.writeMapStart();
                encoder.setItemCount(map.size());

                for (final Entry<K, V> entry : map.entrySet()) {
                    encoder.startItem();
                    writeKeyValue(encoder, entry);
                }

                encoder.writeMapEnd();

                encoder.flush();
                byteArrayOutputStream.close();

                set(Base64.encodeBase64String(byteArrayOutputStream.toByteArray()));
            } catch (final IOException _ioException) {
                throw new RuntimeException("Problems serializing MapProperty [" + this + "] on map [" + Arrays.toString(map.entrySet().toArray()) + "]: " + _ioException.getMessage(), _ioException);
            }
        }
    }

    public Map<K, V> getMap() {
        if (cache == null) {
            cache = new HashMap<>();
            final String data = get();

            if (data != null && data.length() > 0) {
                final Decoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(Base64.decodeBase64(data)), reusableBinaryDecoder);

                try {
                    for (long i = decoder.readMapStart(); i != 0; i = decoder.mapNext()) {
                        for (long j = 0; j < i; j++) {
                            final Entry<K, V> entry = readKeyValue(decoder);
                            cache.put(entry.getKey(), entry.getValue());
                        }
                    }
                } catch (final IOException _ioException) {
                    throw new RuntimeException("Problems deserializing MapProperty [" + this + "] with data [" + data + "]: " + _ioException.getMessage(), _ioException);
                }
            }
        }
        return cache;
    }


    protected abstract void writeKeyValue(final Encoder encoder, final Entry<K, V> entry) throws IOException;

    protected abstract Map.Entry<K, V> readKeyValue(final Decoder decoder) throws IOException;


    // Reading operations
    public int size() {
        return getMap().size();
    }

    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    public boolean containsKey(final Object key) {
        return getMap().containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return getMap().containsValue(value);
    }

    public V get(final Object key) {
        return getMap().get(key);
    }

    public Set<K> keySet() {
        return getMap().keySet();
    }

    public Collection<V> values() {
        return getMap().values();
    }

    public Set<Entry<K, V>> entrySet() {
        return getMap().entrySet();
    }

    // Modification Operations

    public V put(final K key, final V value) {
        return getMap().put(key, value);
    }

    public V remove(final Object key) {
        return getMap().remove(key);
    }

    public void putAll(final Map<? extends K, ? extends V> m) {
        getMap().putAll(m);
    }

    public void clear() {
        getMap().clear();
    }
}
