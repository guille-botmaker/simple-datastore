package com.zupcat.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * It adds features to JSONObject
 */
public class DataObject extends JSONObject {

    public DataObject() {
    }

    public DataObject(final Map map) {
        super(map);
    }

    public DataObject(final String source) throws JSONException {
        super(source);
    }

    public DataObject(final JSONObject jo, final String[] names) {
        super(jo, names);
    }

    public boolean isFullyEquals(final DataObject other) {
        return other != null && this.toString().equals(other.toString());
    }

    public void mergeWith(final DataObject other) {
        // TODO IMPLEMENT!!
    }
}
