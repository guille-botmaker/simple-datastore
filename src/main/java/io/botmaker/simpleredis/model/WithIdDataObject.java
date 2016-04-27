package io.botmaker.simpleredis.model;

import org.json.JSONException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * It adds features to JSONObject
 */
public class WithIdDataObject extends DataObject {

    public static final String ID_KEY = "_id_";
    private static final long serialVersionUID = 471847964351314234L;

    public WithIdDataObject() {
    }

    public WithIdDataObject(final Map map) {
        super(map);
    }

    public WithIdDataObject(final WithIdDataObject another) {
        super(another);
    }

    public WithIdDataObject(final String source) throws JSONException {
        super(source);
    }

    public String getId() {
        return optString(ID_KEY, null);
    }

    public void setId(final String id) {
        put(ID_KEY, id);
    }

    private void writeObject(final ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeUTF(this.toString());
    }

    private void readObject(final ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
        // default deserialization
        objectInputStream.defaultReadObject();

        this.mergeWith(new WithIdDataObject(objectInputStream.readUTF()));
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
