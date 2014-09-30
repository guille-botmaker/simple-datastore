package com.zupcat.property;

import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.PropertyMeta;
import org.json.JSONArray;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class ListProperty<V> extends PropertyMeta<List<V>> implements Serializable, List<V> {

    private static final long serialVersionUID = 6181606486836703354L;


    public ListProperty(final DatastoreEntity owner) {
        super(owner);
    }


    @Override
    protected List<V> getValueImpl(final DataObject dataObject) {
        return getJSONArrayFrom(dataObject);
    }

    @Override
    protected void setValueImpl(final List<V> value, final DataObject dataObject) {
        if (value == null || value.isEmpty()) {
            dataObject.remove(name);
        } else {
            dataObject.put(name, new JSONArray(value));
        }
    }

    private List<V> getJSONArrayFrom(final DataObject dataObject) {
        final JSONArray jsonArray;

        if (dataObject.has(name)) {
            jsonArray = dataObject.getJSONArray(name);
        } else {
            jsonArray = new JSONArray();
            dataObject.put(name, jsonArray);
        }
        return getInternalListFromJSONArray(jsonArray);
    }

    private List<V> getList() {
        return getJSONArrayFrom(getOwner().getDataObject());
    }

    private List<V> getInternalListFromJSONArray(final JSONArray jsonArray) {
        try {
            final Field arrayField = jsonArray.getClass().getDeclaredField("myArrayList");

            arrayField.setAccessible(true);

            return (List<V>) arrayField.get(jsonArray);
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems when getting JSONArray internal array field using reflection for array [" + jsonArray + ": " + _exception.getMessage(), _exception);
        }
    }


    // Reading operations
    @Override
    public int size() {
        return getList().size();
    }

    @Override
    public boolean isEmpty() {
        return getList().isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return getList().contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return getList().iterator();
    }

    @Override
    public Object[] toArray() {
        return getList().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return getList().toArray(a);
    }


    @Override
    public boolean containsAll(final Collection<?> c) {
        return getList().containsAll(c);
    }

    @Override
    public V get(final int index) {
        return getList().get(index);
    }

    @Override
    public int indexOf(final Object o) {
        return getList().indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return getList().lastIndexOf(o);
    }

    @Override
    public ListIterator<V> listIterator() {
        return getList().listIterator();
    }

    @Override
    public ListIterator<V> listIterator(final int index) {
        return getList().listIterator(index);
    }

    @Override
    public List<V> subList(final int fromIndex, final int toIndex) {
        return getList().subList(fromIndex, toIndex);
    }


    // Writing operations
    @Override
    public boolean add(final V v) {
        return getList().add(v);
    }

    @Override
    public boolean remove(final Object o) {
        return getList().remove(o);
    }

    @Override
    public boolean addAll(final Collection<? extends V> c) {
        return getList().addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends V> c) {
        return getList().addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return getList().removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return getList().retainAll(c);
    }

    @Override
    public void clear() {
        getList().clear();
    }

    @Override
    public V set(final int index, final V element) {
        return getList().set(index, element);
    }

    @Override
    public void add(final int index, final V element) {
        getList().add(index, element);
    }

    @Override
    public V remove(final int index) {
        return getList().remove(index);
    }
}
