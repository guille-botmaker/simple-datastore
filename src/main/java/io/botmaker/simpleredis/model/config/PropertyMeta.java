package io.botmaker.simpleredis.model.config;

import io.botmaker.simpleredis.audit.AuditHandlerServiceFactory;
import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds metadata of busines objects properties
 */
public abstract class PropertyMeta<E> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    private final RedisEntity owner;
    protected String name;
    protected AbstractPropertyBuilder<? extends PropertyMeta<E>, E> options;


    protected PropertyMeta(final RedisEntity owner) {
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

    protected RedisEntity getOwner() {
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
        E result = getValueImpl(owner.getDataObject());

        if (result == null) {
            result = options.initialValue;
            set(result);
        }
        return result;
    }

    public void set(final E value) {
        if (options.inlinePropertyChangeObserver != null) {
            options.inlinePropertyChangeObserver.execute(value);
        }
        this.set(value, options.auditable);
    }

    public void set(final E value, final boolean forceAudit) {
        if (forceAudit) {
            AuditHandlerServiceFactory.getAuditHandler().logPropertyDataChanged(this, value, owner.getId());
        }

        final DataObject dataObject = owner.getDataObject();

        if (value == null || value.equals(options.initialValue)) {
            dataObject.remove(name);
        } else {
            setValueImpl(value, dataObject);
        }
    }

    public AbstractPropertyBuilder<? extends PropertyMeta<E>, E> getOptions() {
        return options;
    }

    protected abstract E getValueImpl(final DataObject dataObject);

    protected abstract void setValueImpl(final E value, final DataObject dataObject);

    public void setFromStringValue(final String stringValue) {
        setFromStringValue(stringValue, options.auditable);
    }

    public abstract void setFromStringValue(final String stringValue, final boolean forceAudit);

    @Override
    public String toString() {
        return "[" + StringUtils.replace(getClass().getName(), "io.botmaker.simpleredis.property.", "") + "|" + name + "->" + get() + "]";
    }
}
