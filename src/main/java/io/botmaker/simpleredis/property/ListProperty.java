package io.botmaker.simpleredis.property;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import org.apache.commons.collections4.list.SetUniqueList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public final class ListProperty<V> extends PropertyMeta<List<V>> implements Serializable, List<V> {

    private static final long serialVersionUID = 6181606486836703354L;

    private final Class<? extends DataObject> itemClass;
    private final boolean keepUniqueElements;

    public ListProperty(final RedisEntity owner, final Class<? extends DataObject> _itemClass, final boolean _keepUniqueElements) {
        super(owner);
        itemClass = _itemClass;
        keepUniqueElements = _keepUniqueElements;
    }

    public Class<? extends DataObject> getItemClass() {
        return itemClass;
    }

    @Override
    public void setFromStringValue(final String stringValue, final boolean forceAudit) {
        try {
            final StringTokenizer stringTokenizer = new StringTokenizer(stringValue, ",");
            final List list = new ArrayList<>(stringTokenizer.countTokens());
            while (stringTokenizer.hasMoreElements()) {
                list.add(stringTokenizer.nextElement().toString().trim());
            }
            set(list, forceAudit);
        } catch (final Exception e) {
            throw new UnsupportedOperationException("ListProperty only support set from string for string list", e);
        }
    }

    @Override
    protected List<V> getValueImpl(final DataObject dataObject) {
        final List result = getJSONArrayFrom(dataObject);

        if (!result.isEmpty() && itemClass != null) {
            final Object firstValue = result.get(0);
            final boolean isSameType = itemClass.isInstance(firstValue);

            if (!isSameType) {
                if (!(firstValue instanceof String) && firstValue != null)
                    new RuntimeException("[FER] ListProperty getValueImpl itemClass [" + itemClass +
                            "] firstValue.getClass [" + firstValue.getClass() + "]" +
                            "] firstValue [" + firstValue + "]").printStackTrace();

                final List<DataObject> tempList = new ArrayList<>(result.size());

                for (final Object v : result) {
                    final DataObject convertedItem;

                    try {
                        convertedItem = itemClass.newInstance();
                    } catch (final Exception _exception) {
                        throw new RuntimeException("Could not instantiate object of class [" + itemClass.getName() + "]. Maybe missing empty constructor?: " + _exception.getMessage(), _exception);
                    }

                    convertedItem.mergeWith((JSONObject) v);
                    tempList.add(convertedItem);
                }

                result.clear();
                result.addAll(tempList);
            }
        }
        return (List<V>) result;
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
        return DataObject.getInternalListFromJSONArray(jsonArray);
    }

    private List<V> getList() {
        return getValueImpl(getOwner().getDataObject());
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
        return keepUniqueElements ? SetUniqueList.setUniqueList(getList()).add(v) : getList().add(v);
    }

    @Override
    public boolean remove(final Object o) {
        return getList().remove(o);
    }

    @Override
    public boolean addAll(final Collection<? extends V> c) {
        return keepUniqueElements ? SetUniqueList.setUniqueList(getList()).addAll(c) : getList().addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends V> c) {
        return keepUniqueElements ? SetUniqueList.setUniqueList(getList()).addAll(index, c) : getList().addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return getList().removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return keepUniqueElements ? SetUniqueList.setUniqueList(getList()).retainAll(c) : getList().retainAll(c);
    }

    @Override
    public void clear() {
        getList().clear();
    }

    @Override
    public V set(final int index, final V element) {
        return keepUniqueElements ? SetUniqueList.setUniqueList(getList()).set(index, element) : getList().set(index, element);
    }

    @Override
    public void add(final int index, final V element) {
        if (keepUniqueElements) {
            SetUniqueList.setUniqueList(getList()).add(index, element);
        } else {
            getList().add(index, element);
        }
    }

    @Override
    public V remove(final int index) {
        return getList().remove(index);
    }
}
