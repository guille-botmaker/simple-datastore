package com.zupcat.model;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.data.RecordBuilder;
import org.apache.avro.specific.AvroGenerated;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.specific.SpecificRecordBuilderBase;
import org.apache.avro.util.Utf8;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents an object
 */
@AvroGenerated
public class ObjectVar extends SpecificRecordBase implements SpecificRecord {

    public static final Schema SCHEMA$ = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ObjectVar\",\"namespace\":\"com.zupcat.model\",\"fields\":[{\"name\":\"vars\",\"type\":[\"null\",{\"type\":\"map\",\"values\":{\"type\":\"record\",\"name\":\"Var\",\"fields\":[{\"name\":\"iv\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"sv\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"bv\",\"type\":[\"null\",\"boolean\"],\"default\":null},{\"name\":\"lv\",\"type\":[\"null\",\"long\"],\"default\":null}]}}],\"default\":null}]}");

    private final Map<CharSequence, Var> vars = new HashMap<>();

    /**
     * Default constructor.
     */
    public ObjectVar() {
    }


    public boolean isFullyEquals(final ObjectVar other) {
        if (other == null || this.vars.size() != other.vars.size()) {
            return false;
        }

        for (final Map.Entry<CharSequence, Var> entry : this.vars.entrySet()) {
            if (!entry.getValue().isFullyEquals(other.vars.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }


    public void setListToStringProperty(final String propertyName, final Collection<String> list) {
        final StringBuilder builder = new StringBuilder(200);
        final Iterator<String> iterator = list.iterator();

        while (iterator.hasNext()) {
            builder.append(iterator.next());

            if (iterator.hasNext()) {
                builder.append("|");
            }
        }
        this.set(propertyName, builder.toString());
    }

    public void clear() {
        vars.clear();
    }

    /**
     * All-args constructor.
     */
    public ObjectVar(final Map<CharSequence, Var> varsMap) {
        this.vars.putAll(varsMap);
    }

    public Schema getSchema() {
        return SCHEMA$;
    }

    // Used by DatumWriter.  Applications should not call.
    public Object get(final int field$) {
        switch (field$) {
            case 0:
                return vars;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value = "unchecked")
    public void put(final int field$, final Object value$) {
        switch (field$) {
            case 0:
                setVars((Map<CharSequence, Var>) value$);
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'vars' field.
     */
    public Map<CharSequence, Var> getVars() {
        return vars;
    }

    /**
     * Sets the value of the 'vars' field.
     *
     * @param value the value to set.
     */
    public void setVars(final Map<CharSequence, Var> value) {
        this.vars.clear();
        this.vars.putAll(value);
    }

    /**
     * Creates a new ObjectVar RecordBuilder
     */
    public static ObjectVar.Builder newBuilder() {
        return new ObjectVar.Builder();
    }

    /**
     * Creates a new ObjectVar RecordBuilder by copying an existing Builder
     */
    public static ObjectVar.Builder newBuilder(final Builder other) {
        return new ObjectVar.Builder(other);
    }

    /**
     * Creates a new ObjectVar RecordBuilder by copying an existing ObjectVar instance
     */
    public static ObjectVar.Builder newBuilder(final ObjectVar other) {
        return new ObjectVar.Builder(other);
    }

    public void mergeWith(final ObjectVar other) {
        if (!other.vars.isEmpty()) {
            for (final Map.Entry<CharSequence, Var> otherEntry : other.vars.entrySet()) {
                this.vars.put(otherEntry.getKey(), otherEntry.getValue());
            }
        }
    }

    public void set(final String varName, final String stringValue) {
        final Var var = new Var();
        var.setSv(stringValue);
        set(varName, var);
    }

    public void set(final String varName, final Integer integerValue) {
        final Var var = new Var();
        var.setIv(integerValue);
        set(varName, var);
    }

    public void set(final String varName, final Boolean booleanValue) {
        final Var var = new Var();
        var.setBv(booleanValue);
        set(varName, var);
    }

    public void set(final String varName, final Long longValue) {
        final Var var = new Var();
        var.setLv(longValue);
        set(varName, var);
    }

    private void set(final String key, final Var var) {
        vars.put(new Utf8(key), var);
    }

    public void removeVar(final String varName) {
        vars.remove(new Utf8(varName));
    }

    public String getString(final String varName) {
        final Var var = getVar(varName);
        return var == null || var.getSv() == null ? null : var.getSv().toString();
    }

    public Integer getInteger(final String varName) {
        final Var var = getVar(varName);
        return var == null || var.getIv() == null ? null : var.getIv();
    }

    public Boolean getBoolean(final String varName) {
        final Var var = getVar(varName);
        return var == null || var.getBv() == null ? null : var.getBv();
    }

    public Long getLong(final String varName) {
        final Var var = getVar(varName);
        return var == null || var.getLv() == null ? null : var.getLv();
    }

    public boolean hasKey(final String varName) {
        return getVar(varName) != null;
    }

    private Var getVar(final String varName) {
        return vars.get(new Utf8(varName));
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(100));
    }

    public String toString(final StringBuilder builder) {
        builder.append("ObjectVar{");
        boolean empty = true;

        for (final Map.Entry<CharSequence, Var> entry : vars.entrySet()) {
            builder.append(entry.getKey()).append("->").append(entry.getValue()).append("|");
            empty = false;
        }

        if (!empty) {
            builder.setLength(builder.length() - 1);
        }
        builder.append("}");

        return builder.toString();
    }


    public static final class Builder extends SpecificRecordBuilderBase<ObjectVar> implements RecordBuilder<ObjectVar> {

        private Map<java.lang.CharSequence, Var> varsMap;

        /**
         * Creates a new Builder
         */
        private Builder() {
            super(ObjectVar.SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder
         */
        private Builder(final ObjectVar.Builder other) {
            super(other);
        }

        /**
         * Creates a Builder by copying an existing ObjectVar instance
         */
        private Builder(final ObjectVar other) {
            super(ObjectVar.SCHEMA$);

            if (isValidValue(fields()[0], other.vars)) {
                this.varsMap = data().deepCopy(fields()[0].schema(), other.vars);
                fieldSetFlags()[0] = true;
            }
        }

        /**
         * Gets the value of the 'vars' field
         */
        public Map<CharSequence, Var> getVarsMap() {
            if (varsMap == null) {
                varsMap = new HashMap<>();
            }
            return varsMap;
        }

        /**
         * Sets the value of the 'vars' field
         */
        public ObjectVar.Builder setVarsMap(final Map<CharSequence, Var> value) {
            validate(fields()[0], value);

            this.varsMap = value;

            fieldSetFlags()[0] = true;

            return this;
        }

        /**
         * Checks whether the 'vars' field has been set
         */
        public boolean hasVarsMap() {
            return fieldSetFlags()[0];
        }

        /**
         * Clears the value of the 'vars' field
         */
        public ObjectVar.Builder clearVarsMap() {
            varsMap = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        public ObjectVar build() {
            try {
                final ObjectVar record = new ObjectVar();
                record.vars.clear();
                record.vars.putAll(fieldSetFlags()[0] ? this.varsMap : (Map<CharSequence, Var>) defaultValue(fields()[0]));
                return record;
            } catch (final Exception e) {
                throw new AvroRuntimeException(e);
            }
        }
    }
}
