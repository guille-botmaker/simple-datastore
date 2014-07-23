package com.zupcat.model;

import com.zupcat.audit.AuditHandlerService;

import java.io.Serializable;

public abstract class PropertyMeta<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    protected final String name;
    protected final PersistentObject owner;
    private final E initialValue;
    private final boolean sentToClient;
    private final boolean auditable;
    private final boolean indexable;


    protected PropertyMeta(final PersistentObject owner, final String name, final E initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable) {
        this.name = name;
        this.owner = owner;
        this.initialValue = initialValue;
        this.sentToClient = sentToClient;
        this.auditable = auditable;
        this.indexable = indexable;
    }

    public E get() {
        final E result = getValueImpl(owner.getObjectHolder().getObjectVar());

        return result == null ? initialValue : result;
    }

    public String getName() {
        return name;
    }

    public boolean isIndexable() {
        return indexable;
    }

    public E getInitialValue() {
        return initialValue;
    }

    public void set(final E value) {
        if (auditable) {
            AuditHandlerService.getAuditHandler().logPropertyDataChanged(this, value, owner.getId());
        }

        final ObjectVar objectVar = owner.getObjectHolder().getObjectVar();

        if (value == null || value.equals(initialValue)) {
            objectVar.removeVar(name);
        } else {
            setValueImpl(value, objectVar);
        }
    }

    protected abstract E getValueImpl(final ObjectVar objectVar);

    protected abstract void setValueImpl(final E value, final ObjectVar objectVar);
}
