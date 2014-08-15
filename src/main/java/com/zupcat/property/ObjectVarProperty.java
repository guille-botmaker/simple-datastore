package com.zupcat.property;

import com.zupcat.model.ObjectVar;
import com.zupcat.model.PersistentObject;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;

public final class ObjectVarProperty<OV extends ObjectVar> extends PropertyMeta<OV> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private transient OV cache;
    private final Class<OV> objectClass;


    public ObjectVarProperty(final PersistentObject owner, final String name, final Class<OV> objectClass) {
        this(owner, name, objectClass, false, false);
    }

    public ObjectVarProperty(final PersistentObject owner, final String name, final Class<OV> _objectClass, final boolean sentToClient, final boolean auditable) {
        super(owner, name, null, sentToClient, auditable, false);

        owner.addPropertyMeta(name, this);

        objectClass = _objectClass;
    }

    @Override
    public OV get() {
        if (cache == null) {
            final ObjectVar container = owner.getObjectHolder().getObjectVar();
            final ObjectVar theObjectVar = container.getObjectVar(name);

            if (theObjectVar != null) {
                buildNewInstance();

                cache.mergeWith(theObjectVar);
            }
        }
        return cache;
    }

    public void set(final OV value) {
        if (value == null) {
            final ObjectVar container = owner.getObjectHolder().getObjectVar();
            container.set(name, ((ObjectVar) null));
            cache = null;
        } else {
            get();

            if (cache == null) {
                buildNewInstance();
            }

            cache.mergeWith(value);
        }
    }

    @Override
    public void commit() {
        super.commit();

        final OV toPersistObjectVar = get();

        if (toPersistObjectVar == null) {
            set(null);
        } else {
            final ObjectVar container = owner.getObjectHolder().getObjectVar();
            container.set(name, cache);
        }
    }

    private void buildNewInstance() {
        try {
            cache = objectClass.newInstance();
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems instantiating class [" + objectClass.getName() + "] (maybe a missing default empty constructor?): " + _exception.getMessage(), _exception);
        }
    }

    @Override
    protected OV getValueImpl(final ObjectVar objectVar) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setValueImpl(final ObjectVar value, final ObjectVar holderObjectVar) {
        throw new UnsupportedOperationException();
    }
}
