package com.zupcat.property;

import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class ListObjectVarProperty<OV extends ObjectVar> extends PropertyMeta<List<? extends OV>> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private transient List<OV> cache;
    private final Class<OV> objectClass;


    public ListObjectVarProperty(final DatastoreEntity owner, final Class<OV> _objectClass) {
        super(owner);

        objectClass = _objectClass;

        owner.addPropertyMeta(name, this);
    }

    @Override
    public List<OV> get() {
        if (cache == null) {
            cache = new ArrayList<>();

            final ObjectVar container = getOwner().getInternalObjectHolder().getObjectVar();
            final ObjectHolder objectHolder = container.getObjectHolder(name);

            if (objectHolder != null) {
                final List<OV> items = (List<OV>) objectHolder.getItems();

                if (items != null && !items.isEmpty()) {
                    for (final OV item : items) {
                        final OV specificItem = buildNewInstance();
                        specificItem.mergeWith(item);

                        cache.add(specificItem);
                    }
                }
            }
        }
        return cache;
    }

    public void set(final List<OV> value) {
        if (value == null || value.isEmpty()) {
            final ObjectVar container = getOwner().getInternalObjectHolder().getObjectVar();
            container.set(name, ((ObjectHolder) null));

            if (cache != null) {
                cache.clear();
            }
        } else {
            get();

            cache.clear();
            cache.addAll(value);
        }
    }


    @Override
    public void commit() {
        super.commit();

        final List<OV> list = get();

        if (list == null || list.isEmpty()) {
            set(null);
        } else {
            final ObjectVar container = getOwner().getInternalObjectHolder().getObjectVar();

            final ObjectHolder objectHolder = new ObjectHolder();

            for (final OV ov : list) {
                objectHolder.addItem(ov);
            }
            container.set(name, objectHolder);
        }
    }

    private OV buildNewInstance() {
        try {
            return objectClass.newInstance();
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems instantiating class [" + objectClass.getName() + "] (maybe a missing default empty constructor?): " + _exception.getMessage(), _exception);
        }
    }

    @Override
    protected List<OV> getValueImpl(final ObjectVar objectVar) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setValueImpl(final List<? extends OV> value, final ObjectVar objectVar) {
        throw new UnsupportedOperationException();
    }
}
