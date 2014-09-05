package com.zupcat.model;

import com.zupcat.audit.AuditHandlerServiceFactory;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds metadata of busines objects properties
 */
public abstract class PropertyMeta<E> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    protected String name;
    protected final DatastoreEntity owner;
    private final E initialValue;
    private final boolean sentToClient;
    private final boolean auditable;
    private final boolean indexable;


    protected PropertyMeta(final DatastoreEntity owner, final E initialValue, final boolean sentToClient, final boolean auditable, final boolean indexable) {
        this.owner = owner;
        this.initialValue = initialValue;
        this.sentToClient = sentToClient;
        this.auditable = auditable;
        this.indexable = indexable;
    }

    public String getPropertyName() {
        return name;
    }

    public void setPropertyName(final String propertyName) {
        this.name = propertyName;
    }

    public boolean isFullyEquals(final PropertyMeta other) {
        // skipping owner comparisson. It is not needed and causes stackoverflow
        return !(other == null ||
                !Objects.equals(this.name, other.name) ||
                !Objects.equals(this.initialValue, other.initialValue) ||
                this.sentToClient != other.sentToClient ||
                this.auditable != other.auditable ||
                this.indexable != other.indexable);
    }

    public boolean isIndexable() {
        return indexable;
    }

    public E getInitialValue() {
        return initialValue;
    }

    protected boolean hasToSendToClient() {
        return sentToClient;
    }

    public E get() {
        final E result = getValueImpl(owner.getInternalObjectHolder().getObjectVar());

        return result == null ? initialValue : result;
    }

    /**
     * Called when object is about to become a Datastore Entity
     */
    public void commit() {
        // nothing to do
    }

    public void set(final E value) {
        this.set(value, auditable);
    }

    public void set(final E value, final boolean forceAudit) {
        if (forceAudit) {
            AuditHandlerServiceFactory.getAuditHandler().logPropertyDataChanged(this, value, owner.getId());
        }

        final ObjectVar objectVar = owner.getInternalObjectHolder().getObjectVar();

        if (value == null || value.equals(initialValue)) {
            objectVar.removeVar(name);
        } else {
            setValueImpl(value, objectVar);
        }
    }

    protected abstract E getValueImpl(final ObjectVar objectVar);

    protected abstract void setValueImpl(final E value, final ObjectVar objectVar);
}
