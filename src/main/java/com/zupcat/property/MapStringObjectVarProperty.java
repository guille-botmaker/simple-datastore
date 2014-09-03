package com.zupcat.property;

import com.zupcat.model.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapStringObjectVarProperty<OV extends WithIdObjectVar> extends PropertyMeta<Map<String, ? extends OV>> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;

    private transient Map<String, OV> cache;
    private final Class<OV> objectClass;


    public MapStringObjectVarProperty(final DatastoreEntity owner, final Class<OV> _objectClass) {
        this(owner, _objectClass, false, false);
    }

    public MapStringObjectVarProperty(final DatastoreEntity owner, final Class<OV> _objectClass, final boolean sentToClient, final boolean auditable) {
        super(owner, null, sentToClient, auditable, false);

        objectClass = _objectClass;

        owner.addPropertyMeta(name, this);
    }

    @Override
    public Map<String, OV> get() {
        if (cache == null) {
            cache = new HashMap<>();

            final ObjectVar container = owner.getObjectHolder().getObjectVar();
            final ObjectHolder objectHolder = container.getObjectHolder(name);

            if (objectHolder != null) {
                final List<ObjectVar> items = objectHolder.getItems();

                if (items != null && !items.isEmpty()) {
                    for (final ObjectVar item : items) {
                        final OV specificItem = buildNewInstance();
                        specificItem.mergeWith(item);

                        cache.put(specificItem.getId(), specificItem);
                    }
                }
            }
        }
        return cache;
    }

    public void set(final Map<String, OV> value) {
        if (value == null || value.isEmpty()) {
            final ObjectVar container = owner.getObjectHolder().getObjectVar();
            container.set(name, ((ObjectHolder) null));

            if (cache != null) {
                cache.clear();
            }
        } else {
            get();

            cache.clear();
            cache.putAll(value);
        }
    }


    @Override
    public void commit() {
        super.commit();

        final Map<String, OV> map = get();

        if (map == null || map.isEmpty()) {
            set(null);
        } else {
            final ObjectVar container = owner.getObjectHolder().getObjectVar();

            final ObjectHolder objectHolder = new ObjectHolder();

            for (final OV ov : map.values()) {
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
    protected Map<String, ? extends OV> getValueImpl(final ObjectVar objectVar) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setValueImpl(final Map<String, ? extends OV> value, final ObjectVar objectVar) {
        throw new UnsupportedOperationException();
    }
}
