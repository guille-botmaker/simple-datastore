package com.zupcat.model;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class DataObjectSerializer<T extends DataObject> implements Serializable {

    private static final long serialVersionUID = 471847964351314234L;


    public void serializeTo(final T record, final boolean compressing, final OutputStream outputStream) {
        final Writer writer = compressing ? new OutputStreamWriter(new DeflaterOutputStream(outputStream)) : new OutputStreamWriter(outputStream);

        record.write(writer);
    }

    public byte[] serialize(final T record, final boolean compressing) {
        // ByteArrayOutputStream doesn't need flush nor close
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(10240);

        serializeTo(record, compressing, byteOutputStream);

        return byteOutputStream.toByteArray();
    }

    public void deserialize(final byte[] bytes, final T recordInstance, final boolean compressed) {
        deserialize(new ByteArrayInputStream(bytes), recordInstance, compressed);
    }

    public void deserialize(final InputStream inputStream, final T recordInstance, final boolean compressed) {
        final BufferedReader s = new BufferedReader(new InputStreamReader(compressed ? new InflaterInputStream(inputStream) : inputStream));

        System.err.println("check!");

        final String content = "";

        recordInstance.mergeWith(new DataObject(content));
    }
}
