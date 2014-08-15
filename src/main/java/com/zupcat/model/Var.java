package com.zupcat.model;

import org.apache.avro.Schema;
import org.apache.avro.data.RecordBuilder;
import org.apache.avro.specific.AvroGenerated;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.specific.SpecificRecordBuilderBase;

import java.io.Serializable;
import java.util.Objects;

/**
 * Avro Object that supports having a types variable.
 */
@AvroGenerated
public final class Var extends SpecificRecordBase implements SpecificRecord, Serializable {

    private static final long serialVersionUID = 471847964351314234L;

    public static final Schema SCHEMA$ = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Var\",\"namespace\":\"com.zupcat.model\",\"fields\":[{\"name\":\"iv\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"sv\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"bv\",\"type\":[\"null\",\"boolean\"],\"default\":null},{\"name\":\"lv\",\"type\":[\"null\",\"long\"],\"default\":null}]}");

    private Integer iv;
    private CharSequence sv;
    private Boolean bv;
    private Long lv;


    /**
     * Default constructor.
     */
    public Var() {
    }

    /**
     * All-args constructor.
     */
    public Var(final Integer integerVar, final CharSequence stringVar, final Boolean booleanVar, final Long longVar) {
        this.iv = integerVar;
        this.sv = stringVar;
        this.bv = booleanVar;
        this.lv = longVar;
    }

    public Schema getSchema() {
        return SCHEMA$;
    }

    // Used by DatumWriter.  Applications should not call.
    public Object get(final int field$) {
        switch (field$) {
            case 0:
                return iv;
            case 1:
                return sv;
            case 2:
                return bv;
            case 3:
                return lv;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value = "unchecked")
    public void put(final int field$, final Object value$) {
        switch (field$) {
            case 0:
                iv = (Integer) value$;
                break;
            case 1:
                sv = (CharSequence) value$;
                break;
            case 2:
                bv = (Boolean) value$;
                break;
            case 3:
                lv = (Long) value$;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public boolean isFullyEquals(final Var other) {
        return
                other != null &&
                        Objects.equals(this.iv, other.iv) &&
                        Objects.equals(this.getSv(), other.getSv()) &&
                        Objects.equals(this.bv, other.bv) &&
                        Objects.equals(this.lv, other.lv);
    }

    /**
     * Gets the value of the 'iv' field.
     */
    public Integer getIv() {
        return iv;
    }

    /**
     * Sets the value of the 'iv' field.
     *
     * @param value the value to set.
     */
    public void setIv(final Integer value) {
        this.iv = value;
    }

    /**
     * Gets the value of the 'sv' field.
     */
    public CharSequence getSv() {
        return sv == null ? null : sv.toString();
    }

    /**
     * Sets the value of the 'sv' field.
     *
     * @param value the value to set.
     */
    public void setSv(final CharSequence value) {
        this.sv = value;
    }

    /**
     * Gets the value of the 'bv' field.
     */
    public Boolean getBv() {
        return bv;
    }

    /**
     * Sets the value of the 'bv' field.
     *
     * @param value the value to set.
     */
    public void setBv(final Boolean value) {
        this.bv = value;
    }

    /**
     * Gets the value of the 'lv' field.
     */
    public Long getLv() {
        return lv;
    }

    /**
     * Sets the value of the 'lv' field.
     *
     * @param value the value to set.
     */
    public void setLv(final Long value) {
        this.lv = value;
    }

    /**
     * Creates a new Var RecordBuilder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a new Var RecordBuilder by copying an existing Builder
     */
    public static Builder newBuilder(final Builder other) {
        return new Builder(other);
    }

    /**
     * Creates a new Var RecordBuilder by copying an existing Var instance
     */
    public static Builder newBuilder(final Var other) {
        return new Builder(other);
    }


    @Override
    public String toString() {
        if (iv != null) {
            return "Var.Integer[" + iv + "]";
        }
        if (sv != null) {
            return "Var.String[" + getSv() + "]";
        }
        if (bv != null) {
            return "Var.Boolean[" + bv + "]";
        }
        if (lv != null) {
            return "Var.Long[" + lv + "]";
        } else {
            return "Var.UnknownType[null]";
        }
    }

    /**
     * RecordBuilder for Var instances.
     */
    public static final class Builder extends SpecificRecordBuilderBase<Var> implements RecordBuilder<Var>, Serializable {

        private static final long serialVersionUID = 471847964351314234L;

        private Integer integerVar;
        private CharSequence stringVar;
        private Boolean booleanVar;
        private Long longVar;

        /**
         * Creates a new Builder
         */
        private Builder() {
            super(SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder
         */
        private Builder(final Builder other) {
            super(other);
        }

        /**
         * Creates a Builder by copying an existing Var instance
         */
        private Builder(final Var other) {
            super(SCHEMA$);

            if (isValidValue(fields()[0], other.iv)) {
                this.integerVar = data().deepCopy(fields()[0].schema(), other.iv);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.sv)) {
                this.stringVar = data().deepCopy(fields()[1].schema(), other.sv);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.bv)) {
                this.booleanVar = data().deepCopy(fields()[2].schema(), other.bv);
                fieldSetFlags()[2] = true;
            }
            if (isValidValue(fields()[3], other.lv)) {
                this.longVar = data().deepCopy(fields()[3].schema(), other.lv);
                fieldSetFlags()[3] = true;
            }
        }

        /**
         * Gets the value of the 'iv' field
         */
        public Integer getIntegerVar() {
            return integerVar;
        }

        /**
         * Sets the value of the 'iv' field
         */
        public Builder setIntegerVar(final Integer value) {
            validate(fields()[0], value);
            this.integerVar = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'iv' field has been set
         */
        public boolean hasIntegerVar() {
            return fieldSetFlags()[0];
        }

        /**
         * Clears the value of the 'iv' field
         */
        public Builder clearIntegerVar() {
            integerVar = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'sv' field
         */
        public CharSequence getStringVar() {
            return stringVar;
        }

        /**
         * Sets the value of the 'sv' field
         */
        public Builder setStringVar(final CharSequence value) {
            validate(fields()[1], value);
            this.stringVar = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'sv' field has been set
         */
        public boolean hasStringVar() {
            return fieldSetFlags()[1];
        }

        /**
         * Clears the value of the 'sv' field
         */
        public Builder clearStringVar() {
            stringVar = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /**
         * Gets the value of the 'bv' field
         */
        public Boolean getBooleanVar() {
            return booleanVar;
        }

        /**
         * Sets the value of the 'bv' field
         */
        public Builder setBooleanVar(final Boolean value) {
            validate(fields()[2], value);
            this.booleanVar = value;
            fieldSetFlags()[2] = true;
            return this;
        }

        /**
         * Checks whether the 'bv' field has been set
         */
        public boolean hasBooleanVar() {
            return fieldSetFlags()[2];
        }

        /**
         * Clears the value of the 'bv' field
         */
        public Builder clearBooleanVar() {
            booleanVar = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        /**
         * Gets the value of the 'lv' field
         */
        public Long getLongVar() {
            return longVar;
        }

        /**
         * Sets the value of the 'lv' field
         */
        public Builder setLongVar(final Long value) {
            validate(fields()[3], value);
            this.longVar = value;
            fieldSetFlags()[3] = true;
            return this;
        }

        /**
         * Checks whether the 'lv' field has been set
         */
        public boolean hasLongVar() {
            return fieldSetFlags()[3];
        }

        /**
         * Clears the value of the 'lv' field
         */
        public Builder clearLongVar() {
            longVar = null;
            fieldSetFlags()[3] = false;
            return this;
        }

        @Override
        public Var build() {
            try {
                final Var record = new Var();
                record.iv = fieldSetFlags()[0] ? this.integerVar : (Integer) defaultValue(fields()[0]);
                record.sv = fieldSetFlags()[1] ? this.stringVar : (CharSequence) defaultValue(fields()[1]);
                record.bv = fieldSetFlags()[2] ? this.booleanVar : (Boolean) defaultValue(fields()[2]);
                record.lv = fieldSetFlags()[3] ? this.longVar : (Long) defaultValue(fields()[3]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
