package com.zupcat.model;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.data.RecordBuilder;
import org.apache.avro.specific.AvroGenerated;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.specific.SpecificRecordBuilderBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Avro class for holding complex graphs of objects and serializing it with low footprint
 */
@AvroGenerated
public class ObjectHolder extends SpecificRecordBase implements SpecificRecord, Serializable {

    private static final long serialVersionUID = 471847964351314234L;

    public static final Schema SCHEMA$ = new Schema.Parser().parse("{\"type\": \"record\", \"name\": \"ObjectHolder\", \"namespace\": \"com.zupcat.model\", \"fields\": [ { \"name\": \"objectsList\", \"type\": [\"null\", { \"type\": \"array\", \"items\": { \"type\": \"record\", \"name\": \"ObjectVar\", \"fields\": [ { \"name\": \"vars\", \"type\": [ \"null\", { \"type\": \"map\", \"values\": { \"type\": \"record\", \"name\": \"Var\", \"fields\": [ { \"name\": \"iv\", \"type\": [ \"null\", \"int\" ], \"default\": null }, { \"name\": \"sv\", \"type\": [ \"null\", \"string\" ], \"default\": null }, { \"name\": \"bv\", \"type\": [ \"null\", \"boolean\" ], \"default\": null }, { \"name\": \"lv\", \"type\": [ \"null\", \"long\" ], \"default\": null } ] } } ], \"default\": null } ] }, \"java-class\": \"java.util.List\" }], \"default\": null }, { \"name\": \"objectVar\", \"type\": [\"null\", \"ObjectVar\"], \"default\": null } ] }");

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

    public boolean isFullyEquals(final ObjectHolder other) {
        if (other == null || !this.objectVar.isFullyEquals(other.objectVar) || this.objectsList.size() != other.objectsList.size()) {
            return false;
        }

        for (int i = 0; i < this.objectsList.size(); i++) {
            if (!this.objectsList.get(i).isFullyEquals(other.objectsList.get(i))) {
                return false;
            }
        }
        return true;
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

    public void addItems(final List<ObjectVar> items) {
        objectsList.addAll(items);
    }

    private void checkIfObjectItemExistAndRemove(final ObjectVar itemToFind) {
        int indexToRemove = -1;
        final String targetId = itemToFind.getString(AvroSerializer.ID_KEY);

        if (!objectsList.isEmpty()) {
            int i = 0;

            for (final ObjectVar item : objectsList) {
                if (targetId.equals(item.getString(AvroSerializer.ID_KEY))) {
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

    /**
     * RecordBuilder for ObjectHolder instances.
     */
    public static final class Builder extends SpecificRecordBuilderBase<ObjectHolder> implements RecordBuilder<ObjectHolder>, Serializable {

        private static final long serialVersionUID = 471847964351314234L;

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
