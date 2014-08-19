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

public abstract class AbstractListAnyProperty<V> extends StringProperty implements Serializable, List<V> {

    private static final long serialVersionUID = 6181606486836703354L;

    private static final BinaryEncoder reusableBinaryEncoder = EncoderFactory.get().binaryEncoder(new ByteArrayOutputStream(), null);
    private static final BinaryDecoder reusableBinaryDecoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(new byte[1]), null);

    // to avoid some extra parsing to String
    private transient List<V> cache;


    public AbstractListAnyProperty(final DatastoreEntity owner, final boolean sentToClient, final boolean auditable) {
        super(owner, null, sentToClient, auditable, false);
    }

    @Override
    protected void setValueImpl(final String value, final ObjectVar objectVar) {
        objectVar.set(name, value);
    }

    @Override
    public void commit() {
        super.commit();

        final List<V> list = getList();

        if (list.isEmpty()) {
            set(null);
        } else {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(500);
            final Encoder encoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, reusableBinaryEncoder);

            try {
                encoder.writeArrayStart();
                encoder.setItemCount(list.size());

                for (final V item : list) {
                    encoder.startItem();
                    writeItem(encoder, item);
                }

                encoder.writeArrayEnd();

                encoder.flush();
                byteArrayOutputStream.close();

                set(Base64.encodeBase64String(byteArrayOutputStream.toByteArray()));
            } catch (final IOException _ioException) {
                throw new RuntimeException("Problems serializing ListProperty [" + this + "] on list [" + Arrays.toString(list.toArray()) + "]: " + _ioException.getMessage(), _ioException);
            }
        }
    }

    private List<V> getList() {
        if (cache == null) {
            cache = new ArrayList<>();
            final String data = get();

            if (data != null && data.length() > 0) {
                final Decoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(Base64.decodeBase64(data)), reusableBinaryDecoder);

                try {
                    for (long i = decoder.readArrayStart(); i != 0; i = decoder.arrayNext()) {
                        for (long j = 0; j < i; j++) {
                            cache.add(readItem(decoder));
                        }
                    }
                } catch (final IOException _ioException) {
                    throw new RuntimeException("Problems deserializing ListProperty [" + this + "] with data [" + data + "]: " + _ioException.getMessage(), _ioException);
                }
            }
        }
        return cache;
    }


    protected abstract void writeItem(final Encoder encoder, final V item) throws IOException;

    protected abstract V readItem(final Decoder decoder) throws IOException;


    // Reading operations
    @Override
    public int size() {
        return getList().size();
    }

    @Override
    public boolean isEmpty() {
        return getList().isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return getList().contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return getList().iterator();
    }

    @Override
    public Object[] toArray() {
        return getList().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return getList().toArray(a);
    }


    @Override
    public boolean containsAll(final Collection<?> c) {
        return getList().containsAll(c);
    }

    @Override
    public V get(final int index) {
        return getList().get(index);
    }

    @Override
    public int indexOf(final Object o) {
        return getList().indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return getList().lastIndexOf(o);
    }

    @Override
    public ListIterator<V> listIterator() {
        return getList().listIterator();
    }

    @Override
    public ListIterator<V> listIterator(final int index) {
        return getList().listIterator(index);
    }

    @Override
    public List<V> subList(final int fromIndex, final int toIndex) {
        return getList().subList(fromIndex, toIndex);
    }


    // Writing operations
    @Override
    public boolean add(final V v) {
        return getList().add(v);
    }

    @Override
    public boolean remove(final Object o) {
        return getList().remove(o);
    }

    @Override
    public boolean addAll(final Collection<? extends V> c) {
        return getList().addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends V> c) {
        return getList().addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return getList().removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return getList().retainAll(c);
    }

    @Override
    public void clear() {
        getList().clear();
    }

    @Override
    public V set(final int index, final V element) {
        return getList().set(index, element);
    }

    @Override
    public void add(final int index, final V element) {
        getList().add(index, element);
    }

    @Override
    public V remove(final int index) {
        return getList().remove(index);
    }
}
