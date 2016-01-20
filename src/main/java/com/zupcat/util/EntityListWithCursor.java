package com.zupcat.util;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.repackaged.com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

public final class EntityListWithCursor<T> extends ArrayList<T> implements QueryResultList<T> {

    private Cursor cursor;

    public EntityListWithCursor() {
        super(200);
    }

    @Override
    public List<Index> getIndexList() {
        return null;
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }

    public void setEndCursor(final ByteString endCursor) {
        if (endCursor == null) {
            cursor = null;
        } else {
            cursor = Cursor.fromWebSafeString(BaseEncoding.base64Url().omitPadding().encode(endCursor.toByteArray()));
        }
    }
}
