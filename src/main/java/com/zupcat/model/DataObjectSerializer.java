package com.zupcat.model;

import com.zupcat.dao.SerializationHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class DataObjectSerializer<T extends DataObject> implements Serializable {

    private static final long serialVersionUID = 471847964351314234L;

    private final Charset charset = Charset.forName("UTF-8");

    public byte[] serialize(final T record, final boolean compressing) {
        // ByteArrayOutputStream doesn't need flush nor close
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        serializeTo(record, compressing, byteOutputStream);

        return byteOutputStream.toByteArray();
    }

    public void serializeTo(final T record, final boolean compressing, final OutputStream outputStream) {
        final OutputStream stream = compressing ? new DeflaterOutputStream(outputStream) : outputStream;

        final StringWriter stringWriter = new StringWriter(10240); //10k
        record.write(stringWriter);
        final String content = stringWriter.toString();

        try {
            stream.write(content.getBytes(charset));
            stream.close();

        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems serializing record [" + record + "]: " + _ioException.getMessage(), _ioException);
        }
    }

    public void deserialize(final byte[] bytes, final T recordInstance, final boolean compressed) {
        deserialize(new ByteArrayInputStream(bytes), recordInstance, compressed);
    }

    public void deserialize(final InputStream inputStream, final T recordInstance, final boolean compressed) {
        final InputStream stream = compressed ? new InflaterInputStream(inputStream) : inputStream;

        String content;

        try {
            content = new String(IOUtils.toByteArray(stream), charset);
        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems deserializing record: " + _ioException.getMessage(), _ioException);
        }
        recordInstance.mergeWith(new DataObject(content));
    }

    public void serializeList(final Map<Class, List<T>> records, final ObjectOutputStream objectOutputStream) {
        try {
            serializeListImpl(records, objectOutputStream);

        } catch (final IOException _exception) {
            throw new RuntimeException("Problems writing to stream: " + _exception.getMessage(), _exception);
        }
    }

    private void serializeListImpl(final Map<Class, List<T>> recordsMap, final ObjectOutputStream objectOutputStream) throws IOException {
        if (recordsMap == null || recordsMap.isEmpty()) {
            throw new RuntimeException("Could not serialize empty lists");
        }

        final List<DataObjectSerializedData> serializedData = new ArrayList<>(recordsMap.size());
        final List<String> itemsSerializedList = new ArrayList<>(5000);
        final StringWriter stringWriter = new StringWriter(10240); //10k

        for (final Map.Entry<Class, List<T>> entry : recordsMap.entrySet()) {
            final List<T> items = entry.getValue();

            final DataObjectSerializedData data = new DataObjectSerializedData();
            data.itemsQty = items.size();
            data.className = entry.getKey().getName();

            for (final T item : items) {
                stringWriter.getBuffer().setLength(0);
                item.write(stringWriter);

                itemsSerializedList.add(stringWriter.toString());
            }
        }

        objectOutputStream.writeUTF(Base64.encodeBase64String(SerializationHelper.getBytes(serializedData, false)));
        for (final String serializedItem : itemsSerializedList) {
            objectOutputStream.writeUTF(serializedItem);
        }

        objectOutputStream.flush();
    }

    public Map<Class, List<T>> deserializeList(final ObjectInputStream objectInputStream) {
        try {
            return deserializeListImpl(objectInputStream);

        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | IOException _exception) {
            throw new RuntimeException("Problems reading stream: " + _exception.getMessage(), _exception);
        }
    }

    private Map<Class, List<T>> deserializeListImpl(final ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Map<Class, List<T>> result = new HashMap<>();

        final List<DataObjectSerializedData> serializedData = (List<DataObjectSerializedData>) SerializationHelper.getObjectFromBytes(Base64.decodeBase64(objectInputStream.readUTF()), false);

        for (final DataObjectSerializedData data : serializedData) {
            final Class<T> recordClass = (Class<T>) Class.forName(data.className);
            final List<T> list = new ArrayList<>();
            result.put(recordClass, list);

            for (int i = 0; i < data.itemsQty; i++) {
                final String recordValue = objectInputStream.readUTF();

                final T r = recordClass.newInstance();
                r.mergeWith(new DataObject(recordValue));

                list.add(r);
            }
        }
        return result;
    }
}
