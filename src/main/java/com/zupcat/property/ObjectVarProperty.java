package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;

public final class ObjectVarProperty<OV extends ObjectVar> extends PropertyMeta<OV> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private transient OV cache;
    private final Class<OV> objectClass;


    public ObjectVarProperty(final DatastoreEntity owner, final Class<OV> _objectClass) {
        super(owner);

        owner.addPropertyMeta(name, this);

        objectClass = _objectClass;
    }

    @Override
    public OV get() {
        if (cache == null) {
            final ObjectVar container = getOwner().getInternalObjectHolder().getObjectVar();
            final ObjectHolder objectHolder = container.getObjectHolder(name);

            if (objectHolder != null) {
                final ObjectVar theObjectVar = objectHolder.getObjectVar();

                if (theObjectVar != null) {
                    buildNewInstance();

                    cache.mergeWith(theObjectVar);
                }
            }
        }
        return cache;
    }


    public void set(final OV value) {
        if (value == null) {
            final ObjectVar container = getOwner().getInternalObjectHolder().getObjectVar();

            container.set(name, ((ObjectHolder) null));
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
            final ObjectVar container = getOwner().getInternalObjectHolder().getObjectVar();

            final ObjectHolder objectHolder = new ObjectHolder();
            objectHolder.getObjectVar().mergeWith(cache);
            container.set(name, objectHolder);
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
