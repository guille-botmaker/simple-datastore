package com.zupcat.model.config;

import com.zupcat.audit.AuditHandlerServiceFactory;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.ObjectVar;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds metadata of busines objects properties
 */
public abstract class PropertyMeta<E> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    protected String name;
    protected AbstractPropertyBuilder<? extends PropertyMeta<E>, E> options;
    private final DatastoreEntity owner;


    protected PropertyMeta(final DatastoreEntity owner) {
        this.owner = owner;
    }

    public void config(final AbstractPropertyBuilder<? extends PropertyMeta<E>, E> options) {
        this.options = options;
    }

    public String getPropertyName() {
        return name;
    }

    public void setPropertyName(final String propertyName) {
        this.name = propertyName;
    }

    protected DatastoreEntity getOwner() {
        return owner;
    }

    public boolean isFullyEquals(final PropertyMeta other) {
        // skipping owner comparisson. It is not needed and causes stackoverflow
        return !(other == null ||
                !Objects.equals(this.name, other.name) ||
                !Objects.equals(options.initialValue, other.options.initialValue) ||
                options.sendToClient != other.options.sendToClient ||
                options.auditable != other.options.auditable ||
                options.indexable != other.options.indexable);
    }

    public boolean isIndexable() {
        return options.indexable;
    }

//    public E getInitialValue() {
//        return initialValue;
//    }

    public boolean hasToSendToClient() {
        return options.sendToClient;
    }

    public E get() {
        final E result = getValueImpl(owner.getInternalObjectHolder().getObjectVar());

        return result == null ? options.initialValue : result;
    }

    /**
     * Called when object is about to become a Datastore Entity
     */
    public void commit() {
        // nothing to do
    }

    public void set(final E value) {
        this.set(value, options.auditable);
    }

    public void set(final E value, final boolean forceAudit) {
        if (forceAudit) {
            AuditHandlerServiceFactory.getAuditHandler().logPropertyDataChanged(this, value, owner.getId());
        }

        final ObjectVar objectVar = owner.getInternalObjectHolder().getObjectVar();

        if (value == null || value.equals(options.initialValue)) {
            objectVar.removeVar(name);
        } else {
            setValueImpl(value, objectVar);
        }
    }

    protected abstract E getValueImpl(final ObjectVar objectVar);

    protected abstract void setValueImpl(final E value, final ObjectVar objectVar);

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(100);

        builder.
                append("[").
                append(getClass().getName()).
                append("|").
                append(name).
                append("->").
                append(get()).
                append("]");

        return builder.toString();
    }
}
