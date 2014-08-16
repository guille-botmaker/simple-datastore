package com.zupcat.model;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.property.*;
import com.zupcat.util.TimeUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

public abstract class DatastoreEntity extends PersistentObject implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    public static final int MAX_GROUPS = 100;

    public final IntegerProperty GROUP_ID;
    public final LongProperty LAST_MODIFICATION;

    protected DatastoreEntity(final CacheStrategy cacheStrategy) {
        super(cacheStrategy);

        GROUP_ID = integer("gi", null, false, false, true);
        LAST_MODIFICATION = longInt("lm", null, false, false, true);

        GROUP_ID.set(Math.abs(getId().hashCode() % MAX_GROUPS));

        setModified();
    }

    @Override
    public void setModified() {
        LAST_MODIFICATION.set(TimeUtils.buildStandardModificationTime());
    }

    public int getDaysSinceLastModification() {
        return getMinutesSinceLastModification() / (60 * 24);
    }

    public boolean isFullyEquals(final DatastoreEntity other) {
        return super.isFullyEquals(other) && !(other == null || !Objects.equals(this.GROUP_ID.get(), other.GROUP_ID.get()) || !Objects.equals(this.LAST_MODIFICATION.get(), other.LAST_MODIFICATION.get()));
    }


    public int getMinutesSinceLastModification() {
        final long lm = LAST_MODIFICATION.get();

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
            return new SimpleDateFormat(DATE_FORMAT).parse(Long.toString(LAST_MODIFICATION.get()));
        } catch (final ParseException _parseException) {
            throw new RuntimeException("Problems parsing date with value [" + LAST_MODIFICATION.get() + "]. Expected format [" + DATE_FORMAT + "]: " + _parseException.getMessage(), _parseException);
        }
    }

    public boolean hasAccessedToday() {
        final String lastAccessDate = Long.toString(LAST_MODIFICATION.get()).substring(0, 6);
        final String today = Long.toString(TimeUtils.buildStandardModificationTime()).substring(0, 6);

        return lastAccessDate.equals(today);
    }
    // === Properties factories helpers ==========================================================================================================

    protected StringProperty string(final String name, final String initialValue) {
        return string(name, initialValue, false, false, false);
    }

    protected StringProperty string(final String name, final String initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final StringProperty p = new StringProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }

    protected IntegerProperty integer(final String name, final Integer initialValue) {
        return integer(name, initialValue, false, false, false);
    }

    protected IntegerProperty integer(final String name, final Integer initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final IntegerProperty p = new IntegerProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }

    protected BooleanProperty bool(final String name, final Boolean initialValue) {
        return bool(name, initialValue, false, false, false);
    }

    protected BooleanProperty bool(final String name, final Boolean initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final BooleanProperty p = new BooleanProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }

    protected LongProperty longInt(final String name, final Long initialValue) {
        return longInt(name, initialValue, false, false, false);
    }

    protected LongProperty longInt(final String name, final Long initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final LongProperty p = new LongProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }

    protected ListIntegerProperty listInteger(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final ListIntegerProperty p = new ListIntegerProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected ListLongProperty listLong(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final ListLongProperty p = new ListLongProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected ListStringProperty listString(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final ListStringProperty p = new ListStringProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected MapStringStringProperty mapStringString(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final MapStringStringProperty p = new MapStringStringProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected MapStringIntegerProperty mapStringInteger(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final MapStringIntegerProperty p = new MapStringIntegerProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected MapIntegerIntegerProperty mapIntegerInteger(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final MapIntegerIntegerProperty p = new MapIntegerIntegerProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected MapIntegerStringProperty mapIntegerString(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final MapIntegerStringProperty p = new MapIntegerStringProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected MapStringLongProperty mapStringLong(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final MapStringLongProperty p = new MapStringLongProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }


    protected MapStringMapStringIntegerProperty mapStringMapStringInteger(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final MapStringMapStringIntegerProperty p = new MapStringMapStringIntegerProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }

    protected MapStringMapStringStringProperty mapStringMapStringString(final String name, final boolean defaultSentToClient, final boolean auditable) {
        final MapStringMapStringStringProperty p = new MapStringMapStringStringProperty(this, name, defaultSentToClient, auditable);

        addPropertyMeta(name, p);

        return p;
    }
}
