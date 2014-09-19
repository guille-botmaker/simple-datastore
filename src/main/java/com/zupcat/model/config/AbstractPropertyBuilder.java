package com.zupcat.model.config;

import java.io.Serializable;

public abstract class AbstractPropertyBuilder<P extends PropertyMeta, T> implements Serializable {

    private static final long serialVersionUID = -2702019046191004750L;

    protected boolean indexable;
    protected boolean sendToClient;
    protected boolean auditable;
    protected boolean toLowerCase;
    protected boolean mandatory;
    protected T initialValue;

    private final P propertyMeta;


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

    public boolean isToLowerCase() {
        return toLowerCase;
    }
}
