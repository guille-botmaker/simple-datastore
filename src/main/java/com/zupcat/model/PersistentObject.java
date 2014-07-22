package com.zupcat.model;

import com.google.appengine.api.datastore.KeyFactory;
import com.zupcat.util.RandomUtils;
import com.zupcat.util.TimeUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class PersistentObject implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    private static final int MAX_GROUPS = 100;

    public static final String DATE_FORMAT = "yyMMddHHmmssSSS";

    private String id;
    private long groupId;
    private long lastModification;
    private final ObjectHolder objectHolder = new ObjectHolder();
    private final String entityName;
    private final boolean useSessionCache;


    protected PersistentObject(final boolean useSessionCache) {
        final String className = this.getClass().getName();

        this.useSessionCache = useSessionCache;
        entityName = className.substring(className.lastIndexOf("."));
        id = KeyFactory.createKeyString(entityName, RandomUtils.getInstance().getRandomLong());
        groupId = id.hashCode() % MAX_GROUPS;
        setModified();
    }

    public void setModified() {
        this.lastModification = TimeUtils.buildStandardModificationTime();
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public long getLastModification() {
        return lastModification;
    }

    public long getGroupId() {
        return groupId;
    }

    /**
     * For framework internal calls. Do not use this method directly
     */
    public ObjectHolder getObjectHolder() {
        return objectHolder;
    }

    public int getDaysSinceLastModification() {
        return getMinutesSinceLastModification() / (60 * 24);
    }

    public int getMinutesSinceLastModification() {
        final long lm = getLastModification();

        if (lm == 0l) {
            return 0;
        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        final GregorianCalendar now = new GregorianCalendar();
        final GregorianCalendar modified = new GregorianCalendar();

        try {
            now.setTime(dateFormat.parse(Long.toString(TimeUtils.buildStandardModificationTime())));
            modified.setTime(dateFormat.parse(Long.toString(lm)));

        } catch (final ParseException _parseException) {
            throw new RuntimeException("Problems when getting minutesSinceLastModification with value [" + lm + "]. Expected format [" + DATE_FORMAT + "]: " + _parseException.getMessage(), _parseException);
        }

        return ((int) (Math.abs((now.getTime().getTime() - modified.getTime().getTime())) / 60000));
    }

    public Date getLastModificationAsDate() {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(Long.toString(getLastModification()));
        } catch (final ParseException _parseException) {
            throw new RuntimeException("Problems parsing date with value [" + getLastModification() + "]. Expected format [" + DATE_FORMAT + "]: " + _parseException.getMessage(), _parseException);
        }
    }

    public boolean hasAccessedToday() {
        final String lastAccessDate = Long.toString(this.lastModification).substring(0, 6);
        final String today = Long.toString(TimeUtils.buildStandardModificationTime()).substring(0, 6);

        return lastAccessDate.equals(today);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(500);

        builder
                .append("[")
                .append(entityName)
                .append("|")
                .append(id)
                .append("|")
                .append(lastModification)
                .append("|")
                .append(objectHolder.toString(builder));

        builder.append("]");

        return builder.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PersistentObject that = (PersistentObject) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
