package com.zupcat.dao;

import bsh.Interpreter;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultList;
import com.zupcat.model.PersistentObject;
import com.zupcat.service.SimpleDatastoreServiceFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MassiveDownload implements Serializable {

    private static final long serialVersionUID = 4221412643513234232L;

    private byte[] resultBytes;
    private int pageSize = 100;
    private boolean hasMore;
    private boolean onlyUseGroupId;

    private String webCursor;
    private long fromFormattedTime;
    private int groupId;
    private String kind;
    private String bshFilter = null;


    public void setBshFilter(final String bshFilter) {
        this.bshFilter = bshFilter;
    }

    public void setKind(final String kind) {
        this.kind = kind;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public void setFromFormattedTime(final long fromFormattedTime) {
        this.fromFormattedTime = fromFormattedTime;
    }

    public void setGroupId(final int groupId) {
        this.groupId = groupId;
    }

    public Collection<PersistentObject> getResults() {
        final List<Entity> results = getResultsEntities();

        if (results == null) {
            return Collections.EMPTY_LIST;
        }

        final List<PersistentObject> list = new ArrayList<>(results.size());
        final DAO dao = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getDAO(kind);

        for (final Entity entity : results) {
            list.add(dao.buildPersistentObjectFromEntity(entity));
        }
        return list;
    }

    public List<Entity> getResultsEntities() {
        return resultBytes == null ? Collections.EMPTY_LIST : (List<Entity>) SerializationHelper.getObjectFromCompressedBytes(resultBytes);
    }

    public String getKind() {
        return kind;
    }

    public String getWebCursor() {
        return webCursor;
    }

    public long getFromFormattedTime() {
        return fromFormattedTime;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public void setEmpty() {
        resultBytes = null;
        webCursor = null;
        hasMore = false;

        webCursor = null;
    }

    public boolean hasCursor() {
        return webCursor != null;
    }

    private void filterList(final ArrayList<Entity> list) {
        final Interpreter interpreter = new Interpreter();
        final DAO dao = SimpleDatastoreServiceFactory.getSimpleDatastoreService().getDAO(kind);

        CollectionUtils.filter(list, new Predicate<Entity>() {

            public boolean evaluate(final Entity entity) {
                Object contextObject = null;

                try {
                    contextObject = dao.buildPersistentObjectFromEntity(entity);
                    interpreter.set("obj", contextObject);

                    return (Boolean) interpreter.eval(bshFilter);
                } catch (final Exception _exception) {
                    throw new RuntimeException("Problems when processing BeanShell command [" + bshFilter + "] on object [" + contextObject + "]: " + _exception.getMessage(), _exception);
                }
            }
        });
    }

    public void setResult(final QueryResultList<Entity> result, final Cursor cursor, final boolean noMore) {
        final ArrayList<Entity> list = new ArrayList<Entity>(result);

        if (bshFilter != null) {
            filterList(list);
        }

        resultBytes = SerializationHelper.getCompressedBytes(list);
        hasMore = !noMore;
        webCursor = cursor.toWebSafeString();
    }

    public void prepareForRequesting() {
        resultBytes = null;
    }

    public void setOnlyUseGroupId() {
        this.onlyUseGroupId = true;
    }

    public boolean getOnlyUseGroupId() {
        return onlyUseGroupId;
    }
}
