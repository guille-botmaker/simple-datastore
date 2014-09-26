package com.zupcat.model;

import org.json.JSONException;

import java.util.Map;

/**
 * It adds features to JSONObject
 */
public class WithIdDataObject extends DataObject {

    private static final long serialVersionUID = 471847964351314234L;

    public static final String ID_KEY = "_i";


    public WithIdDataObject() {
    }

    public WithIdDataObject(final Map map) {
        super(map);
    }

    public WithIdDataObject(final String source) throws JSONException {
        super(source);
    }

    public String getId() {
        return getString(ID_KEY);
    }

    public void setId(final String id) {
        put(ID_KEY, id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof WithIdDataObject)) return false;

        final WithIdDataObject that = (WithIdDataObject) o;

        final String myId = getId();
        final String thatId = that.getId();

        return !(myId != null ? !myId.equals(thatId) : thatId != null);
    }

    @Override
    public int hashCode() {
        final String myId = getId();

        return myId != null ? myId.hashCode() : 0;
    }
}
