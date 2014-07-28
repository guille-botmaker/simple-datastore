package com.zupcat.property;

import com.zupcat.model.ObjectVar;
import com.zupcat.model.PersistentObject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

public abstract class AbstractListAnyProperty<V> extends StringProperty implements Serializable, List<V> {

    private static final long serialVersionUID = 6181606486836703354L;

    private static final String ITEMS_SEPARATOR = "/";
    private static final String ITEMS_SEPARATOR_ALT = "[---]";

    // to avoid some extra parsing to String
    private transient List<V> cache;


    public AbstractListAnyProperty(final PersistentObject owner, final String name, final boolean sentToClient, final boolean auditable) {
        super(owner, name, null, sentToClient, auditable, false);
    }

    @Override
    protected void setValueImpl(String value, final ObjectVar objectVar) {
        if (value != null && value.length() > 0) {
            value = StringUtils.replace(value, ITEMS_SEPARATOR, ITEMS_SEPARATOR_ALT);
        }
        objectVar.set(name, value);
    }

    private List<V> getList() {
        if (cache == null) {
            cache = new ArrayList<>();
            String data = get();

            if (data != null && data.length() > 0) {
                data = StringUtils.replace(data, ITEMS_SEPARATOR_ALT, ITEMS_SEPARATOR);

                for (final String entry : StringUtils.split(data, ITEMS_SEPARATOR)) {
                    cache.add(convertItemFromString(entry));
                }
            }
        }
        return cache;
    }


    protected abstract V convertItemFromString(final String s);


    private void upgradeList() {
        final List<V> list = getList();
        final StringBuilder builder = new StringBuilder(100);

        for (final V item : list) {
            builder.append(item).append(ITEMS_SEPARATOR);
        }

        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
            set(builder.toString());
        } else {
            set(null);
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
        final boolean result = getList().add(v);

        upgradeList();

        return result;
    }

    @Override
    public boolean remove(final Object o) {
        final boolean result = getList().remove(o);

        upgradeList();

        return result;
    }

    @Override
    public boolean addAll(final Collection<? extends V> c) {
        final boolean result = getList().addAll(c);

        upgradeList();

        return result;
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends V> c) {
        final boolean result = getList().addAll(index, c);

        upgradeList();

        return result;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        final boolean result = getList().removeAll(c);

        upgradeList();

        return result;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        final boolean result = getList().retainAll(c);

        upgradeList();

        return result;
    }

    @Override
    public void clear() {
        getList().clear();

        set(null);
    }

    @Override
    public V set(final int index, final V element) {
        final V result = getList().set(index, element);

        upgradeList();

        return result;
    }

    @Override
    public void add(final int index, final V element) {
        getList().add(index, element);

        upgradeList();
    }

    @Override
    public V remove(final int index) {
        final V result = getList().remove(index);

        upgradeList();

        return result;
    }
}
