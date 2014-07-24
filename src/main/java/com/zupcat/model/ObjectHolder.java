package com.zupcat.model;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.data.RecordBuilder;
import org.apache.avro.io.*;
import org.apache.avro.specific.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@AvroGenerated
public class ObjectHolder extends SpecificRecordBase implements SpecificRecord {

    private static final byte[] T = {
            0x22 - 0x17,
            0x50 + 0x21,
            0x29 + 0x24,
            0x66 - 0x21,
            0x36 + 0x24,
            0x18 - 0x11,
            0x37 - 0x21,
            0x32 - 0x21,
            0x64 + 0x03,
            0x91 - 0x24,
            0x21
    };

    private static final BinaryEncoder reusableBinaryEncoder = EncoderFactory.get().binaryEncoder(new ByteArrayOutputStream(1024), null);
    private static final BinaryDecoder reusableBinaryDecoder = DecoderFactory.get().binaryDecoder(new byte[1], null);

    public static final Schema SCHEMA$ = new Schema.Parser().parse("{\"type\": \"record\", \"name\": \"ObjectHolder\", \"namespace\": \"com.zupcat.model\", \"fields\": [ { \"name\": \"objectsList\", \"type\": [\"null\", { \"type\": \"array\", \"items\": { \"type\": \"record\", \"name\": \"ObjectVar\", \"fields\": [ { \"name\": \"vars\", \"type\": [ \"null\", { \"type\": \"map\", \"values\": { \"type\": \"record\", \"name\": \"Var\", \"fields\": [ { \"name\": \"iv\", \"type\": [ \"null\", \"int\" ], \"default\": null }, { \"name\": \"sv\", \"type\": [ \"null\", \"string\" ], \"default\": null }, { \"name\": \"bv\", \"type\": [ \"null\", \"boolean\" ], \"default\": null }, { \"name\": \"lv\", \"type\": [ \"null\", \"long\" ], \"default\": null } ] } } ], \"default\": null } ] }, \"java-class\": \"java.util.List\" }], \"default\": null }, { \"name\": \"objectVar\", \"type\": [\"null\", \"ObjectVar\"], \"default\": null } ] }");
    public static final String ID_KEY = "_i";

    private final List<ObjectVar> objectsList = new ArrayList<>();
    private final ObjectVar objectVar = new ObjectVar();

    /**
     * Default constructor.
     */
    public ObjectHolder() {
    }

    /**
     * All-args constructor.
     */
    public ObjectHolder(final List<ObjectVar> objectsList, final ObjectVar objectVar) {
        this.objectsList.addAll(objectsList);
        this.objectVar.mergeWith(objectVar);
    }

    public Schema getSchema() {
        return SCHEMA$;
    }

    // Used by DatumWriter.  Applications should not call.
    public Object get(final int field$) {
        switch (field$) {
            case 0:
                return objectsList;
            case 1:
                return objectVar;
            default:
                throw new AvroRuntimeException("Bad index");
        }
    }

    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value = "unchecked")
    public void put(final int field$, final Object value$) {
        switch (field$) {
            case 0:
                setObjectsList((List<ObjectVar>) value$);
                break;
            case 1:
                setObjectVar((ObjectVar) value$);
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'objectsList' field.
     */
    public List<ObjectVar> getObjectsList() {
        return objectsList;
    }

    /**
     * Sets the value of the 'objectsList' field.
     *
     * @param value the value to set.
     */
    public void setObjectsList(final List<ObjectVar> value) {
        this.objectsList.clear();
        this.objectsList.addAll(value);
    }

    /**
     * Gets the value of the 'objectVar' field.
     */
    public ObjectVar getObjectVar() {
        return objectVar;
    }

    /**
     * Sets the value of the 'objectVar' field.
     *
     * @param value the value to set.
     */
    public void setObjectVar(final ObjectVar value) {
        this.objectVar.clear();
        this.objectVar.mergeWith(value);
    }

    /**
     * Creates a new ObjectHolder RecordBuilder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a new ObjectHolder RecordBuilder by copying an existing Builder
     */
    public static Builder newBuilder(final Builder other) {
        return new Builder(other);
    }

    /**
     * Creates a new ObjectHolder RecordBuilder by copying an existing ObjectHolder instance
     */
    public static Builder newBuilder(final ObjectHolder other) {
        return new Builder(other);
    }

    public ObjectVar getObject() {
        return objectVar;
    }

    public List<ObjectVar> getItems() {
        return objectsList;
    }

    public void addItem(final ObjectVar var) {
        objectsList.add(var);
    }

    public byte[] serialize(final boolean compressing) {
        try {
            return serializeImpl(compressing);
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems serializing ObjectHolder [" + this + "]: " + _exception.getMessage(), _exception);
        }
    }

    private byte[] serializeImpl(final boolean compressing) throws Exception {
        ByteArrayOutputStream byteOutputStream = null;

        try {
            byteOutputStream = new ByteArrayOutputStream(1024);
            final OutputStream outputStream = compressing ? new DeflaterOutputStream(byteOutputStream) : byteOutputStream;

            final Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, reusableBinaryEncoder);
            final SpecificDatumWriter<ObjectHolder> writer = new SpecificDatumWriter<>(ObjectHolder.class);

            writer.write(this, encoder);
            encoder.flush();
            outputStream.close();

            byte[] result = byteOutputStream.toByteArray();
            byteOutputStream.close();
            byteOutputStream = null;

            enc(result);

            return result;

        } finally {
            if (byteOutputStream != null) {
                try {
                    byteOutputStream.close();
                } catch (final IOException _ioException) {
                    // ok to ignore
                }
            }
        }
    }

    private void checkIfObjectItemExistAndRemove(final ObjectVar itemToFind) {
        int indexToRemove = -1;
        final String targetId = itemToFind.getString(ID_KEY);

        if (!objectsList.isEmpty()) {
            int i = 0;

            for (final ObjectVar item : objectsList) {
                if (targetId.equals(item.getString(ID_KEY))) {
                    indexToRemove = i;
                    break;
                }
                i++;
            }
        }

        if (indexToRemove >= 0) {
            objectsList.remove(indexToRemove);
        }
    }

    public void mergeWith(final ObjectHolder other) {
        objectVar.mergeWith(other.objectVar);

        // objectsList merge
        if (!other.objectsList.isEmpty()) {
            for (final ObjectVar otherVar : other.objectsList) {
                checkIfObjectItemExistAndRemove(otherVar);

                objectsList.add(otherVar);
            }
        }
    }

    public static ObjectHolder deserialize(final InputStream inputStream, final boolean compressed) {
        try {
            return deserialize(IOUtils.toByteArray(inputStream), compressed);
        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems when deserializing ObjectHolder: " + _ioException.getMessage(), _ioException);
        }
    }

    public static ObjectHolder deserialize(final byte[] bytes, final boolean compressed) {
        try {
            enc(bytes);

            final SpecificDatumReader<ObjectHolder> reader = new SpecificDatumReader<>(ObjectHolder.class);
            final Decoder decoder = DecoderFactory.get().binaryDecoder(compressed ? new InflaterInputStream(new ByteArrayInputStream(bytes)) : new ByteArrayInputStream(bytes), reusableBinaryDecoder);

            return reader.read(null, decoder);

        } catch (final IOException _ioException) {
            throw new RuntimeException("Problems when deserializing ObjectHolder: " + _ioException.getMessage(), _ioException);
        }
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(500));
    }

    public String toString(final StringBuilder builder) {
        builder.append("ObjectHolder {");
        objectVar.toString(builder);

        if (!objectsList.isEmpty()) {
            builder.append(", Items [");

            for (final ObjectVar item : objectsList) {
                item.toString(builder);

                builder.append("|");
            }
            builder.append("]");
        }
        builder.append("}");

        return builder.toString();
    }


    private static void enc(final byte[] input) {
        final int size = input.length;
        final int tSize = T.length;

        for (int i = 0; i < size; i++) {
            final byte tb = i < tSize ? T[i] : 0x53;

            input[i] = (byte) (input[i] ^ (tb));
        }
    }

    /**
     * RecordBuilder for ObjectHolder instances.
     */
    public static final class Builder extends SpecificRecordBuilderBase<ObjectHolder> implements RecordBuilder<ObjectHolder> {

        private List<ObjectVar> objectsList;
        private ObjectVar objectVar;

        /**
         * Creates a new Builder
         */
        private Builder() {
            super(ObjectHolder.SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder
         */
        private Builder(final Builder other) {
            super(other);
        }

        /**
         * Creates a Builder by copying an existing ObjectHolder instance
         */
        private Builder(final ObjectHolder other) {
            super(ObjectHolder.SCHEMA$);

            if (isValidValue(fields()[0], other.getObjectsList())) {
                this.objectsList = data().deepCopy(fields()[0].schema(), other.getObjectsList());
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.getObjectVar())) {
                this.objectVar = data().deepCopy(fields()[1].schema(), other.getObjectVar());
                fieldSetFlags()[1] = true;
            }
        }

        /**
         * Gets the value of the 'objectsList' field
         */
        public List<ObjectVar> getObjectsList() {
            if (objectsList == null) {
                objectsList = new ArrayList<>();
            }
            return objectsList;
        }

        /**
         * Sets the value of the 'objectsList' field
         */
        public Builder setObjectsList(final List<ObjectVar> value) {
            validate(fields()[0], value);
            this.objectsList = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'objectsList' field has been set
         */
        public boolean hasObjectsList() {
            return fieldSetFlags()[0];
        }

        /**
         * Clears the value of the 'objectsList' field
         */
        public Builder clearObjectsList() {
            objectsList = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'objectVar' field
         */
        public ObjectVar getObjectVar() {
            if (objectVar == null) {
                objectVar = new ObjectVar();
            }
            return objectVar;
        }

        /**
         * Sets the value of the 'objectVar' field
         */
        public Builder setObjectVar(final ObjectVar value) {
            validate(fields()[1], value);
            this.objectVar = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'objectVar' field has been set
         */
        public boolean hasObjectVar() {
            return fieldSetFlags()[1];
        }

        /**
         * Clears the value of the 'objectVar' field
         */
        public Builder clearObjectVar() {
            objectVar = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        public ObjectHolder build() {
            try {
                final ObjectHolder record = new ObjectHolder();
                record.setObjectsList(fieldSetFlags()[0] ? this.getObjectsList() : (List<ObjectVar>) defaultValue(fields()[0]));
                record.setObjectVar(fieldSetFlags()[1] ? this.getObjectVar() : (ObjectVar) defaultValue(fields()[1]));

                return record;
            } catch (final Exception e) {
                throw new AvroRuntimeException(e);
            }
        }
    }
}
