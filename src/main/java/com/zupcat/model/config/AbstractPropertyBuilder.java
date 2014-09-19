package com.zupcat.model.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractPropertyBuilder<P extends PropertyMeta, T> implements Serializable {

    private static final long serialVersionUID = -2702019046191004750L;

    protected boolean indexable;
    protected boolean sendToClient;
    protected boolean auditable;
    protected boolean toLowerCase;
    protected T initialValue;

    private final P propertyMeta;

    // properties attributes
    public boolean mandatory;
    public long numberInclusiveMin;
    public long numberInclusiveMax;
    public int stringMinLength;
    public int stringMaxLength;
    public boolean stringWithoutSpaces;
    public final List<String> stringSpecificValues = new ArrayList<>();


    protected AbstractPropertyBuilder(final P propertyMeta, final T initialValue) {
        this.propertyMeta = propertyMeta;
        this.initialValue = initialValue;
    }

    public P build() {
        propertyMeta.config(this);

        return propertyMeta;
    }

    public AbstractPropertyBuilder<P, T> nonDefaultInitialValue(final T initialValue) {
        this.initialValue = initialValue;

        return this;
    }

    public AbstractPropertyBuilder<P, T> indexable() {
        this.indexable = true;

        return this;
    }

    public AbstractPropertyBuilder<P, T> sendToClient() {
        this.sendToClient = true;

        return this;
    }

    public AbstractPropertyBuilder<P, T> mandatory() {
        this.mandatory = true;

        return this;
    }

    public AbstractPropertyBuilder<P, T> auditable() {
        this.auditable = true;

        return this;
    }

    public AbstractPropertyBuilder<P, T> toLowerCase() {
        this.toLowerCase = true;

        return this;
    }

    public AbstractPropertyBuilder<P, T> numberInclusiveMin(final long v) {
        this.numberInclusiveMin = v;

        return this;
    }

    public AbstractPropertyBuilder<P, T> numberInclusiveMax(final long v) {
        this.numberInclusiveMax = v;

        return this;
    }

    public AbstractPropertyBuilder<P, T> stringMinLength(final int v) {
        this.stringMinLength = v;

        return this;
    }

    public AbstractPropertyBuilder<P, T> stringMaxLength(final int v) {
        this.stringMaxLength = v;

        return this;
    }

    public AbstractPropertyBuilder<P, T> stringWithoutSpaces() {
        this.stringWithoutSpaces = true;

        return this;
    }

    public AbstractPropertyBuilder<P, T> stringSpecificValues(String... values) {
        this.stringSpecificValues.addAll(Arrays.asList(values));

        return this;
    }
}
