package com.zupcat.model.config;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;

import java.io.IOException;
import java.io.Serializable;

public class StringProperty extends PropertyMeta<String> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    private static final int MAGIC_INT = -100;
    private static final long MAGIC_LONG = -100l;
    private static final String MAGIC_STRING = "&";

    public StringProperty(final DatastoreEntity owner) {
        super(owner);
    }

    protected String getValueImpl(final ObjectVar objectVar) {
        return objectVar.getString(name);
    }

    protected void setValueImpl(final String value, final ObjectVar objectVar) {
        objectVar.set(name, (value == null ? null : (options.toLowerCase ? value.toLowerCase() : value)));
    }

    public boolean hasData() {
        final String s = this.get();

        return s != null && s.trim().length() > 0;
    }

    protected Integer readSafeInteger(final Decoder decoder) throws IOException {
        final int i = decoder.readInt();

        if (i == MAGIC_INT) {
            final boolean flag = decoder.readBoolean();

            if (!flag) {
                return null;
            } else {
                return MAGIC_INT;
            }
        } else {
            return i;
        }
    }

    protected void writeSafeInteger(final Encoder encoder, final Integer i) throws IOException {
        if (i == null) {
            encoder.writeInt(MAGIC_INT);
            encoder.writeBoolean(false);
        } else if (i != MAGIC_INT) {
            encoder.writeInt(i);
        } else {
            // if user wants to write a magic number
            encoder.writeInt(MAGIC_INT);
            encoder.writeBoolean(true);
        }
    }

    protected String readSafeString(final Decoder decoder) throws IOException {
        final String s = decoder.readString();

        if (MAGIC_STRING.equals(s)) {
            final boolean flag = decoder.readBoolean();

            if (!flag) {
                return null;
            } else {
                return MAGIC_STRING;
            }
        } else {
            return s;
        }
    }

    protected void writeSafeString(final Encoder encoder, final String s) throws IOException {
        if (s == null) {
            encoder.writeString(MAGIC_STRING);
            encoder.writeBoolean(false);
        } else if (!MAGIC_STRING.equals(s)) {
            encoder.writeString(s);
        } else {
            // if user wants to write a magic number
            encoder.writeString(MAGIC_STRING);
            encoder.writeBoolean(true);
        }
    }

    protected Long readSafeLong(final Decoder decoder) throws IOException {
        final Long l = decoder.readLong();

        if (MAGIC_LONG == l) {
            final boolean flag = decoder.readBoolean();

            if (!flag) {
                return null;
            } else {
                return MAGIC_LONG;
            }
        } else {
            return l;
        }
    }

    protected void writeSafeLong(final Encoder encoder, final Long l) throws IOException {
        if (l == null) {
            encoder.writeLong(MAGIC_LONG);
            encoder.writeBoolean(false);
        } else if (MAGIC_LONG != l) {
            encoder.writeLong(l);
        } else {
            // if user wants to write a magic number
            encoder.writeLong(MAGIC_LONG);
            encoder.writeBoolean(true);
        }
    }
}
