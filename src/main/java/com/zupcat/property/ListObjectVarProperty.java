package com.zupcat.property;

import com.zupcat.model.ObjectHolder;
import com.zupcat.model.ObjectVar;
import com.zupcat.model.PersistentObject;
import com.zupcat.model.PropertyMeta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class ListObjectVarProperty<OV extends ObjectVar> extends PropertyMeta<List<? extends OV>> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private transient List<OV> cache;


    public ListObjectVarProperty(final PersistentObject owner, final String name) {
        this(owner, name, false, false);
    }

    public ListObjectVarProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, null, sentToClient, auditable, false);

        owner.addPropertyMeta(name, this);
    }

    @Override
    public List<OV> get() {
        if (cache == null) {
            cache = new ArrayList<>();

            final ObjectVar container = owner.getObjectHolder().getObjectVar();
            final ObjectHolder objectHolder = container.getObjectHolder(name);

            if (objectHolder != null) {
                final List<OV> items = (List<OV>) objectHolder.getItems();

                if (items != null && !items.isEmpty()) {
                    cache.addAll(items);
                }
            }
        }
        return cache;
    }

    public void set(final List<OV> value) {
        if (value == null || value.isEmpty()) {
            final ObjectVar container = owner.getObjectHolder().getObjectVar();
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
            final ObjectVar container = owner.getObjectHolder().getObjectVar();

            final ObjectHolder objectHolder = new ObjectHolder();

            for (final OV ov : list) {
                objectHolder.addItem(ov);
            }
            container.set(name, objectHolder);
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
