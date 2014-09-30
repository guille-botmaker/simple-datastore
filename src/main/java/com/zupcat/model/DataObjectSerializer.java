package com.zupcat.model;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
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
}
