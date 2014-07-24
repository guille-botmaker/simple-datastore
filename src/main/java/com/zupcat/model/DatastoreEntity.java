package com.zupcat.model;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.property.BooleanProperty;
import com.zupcat.property.IntegerProperty;
import com.zupcat.property.LongProperty;
import com.zupcat.property.StringProperty;
import com.zupcat.util.TimeUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class DatastoreEntity extends PersistentObject implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    public static final int MAX_GROUPS = 100;

    public final IntegerProperty GROUP_ID;
    public final LongProperty LAST_MODIFICATION;

    protected DatastoreEntity(final CacheStrategy cacheStrategy) {
        super(cacheStrategy);

        GROUP_ID = propInt("gi", null, false, false, true);
        LAST_MODIFICATION = propLong("lm", null, false, false, true);

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

    protected StringProperty propString(final String name, final String initialValue) {
        return propString(name, initialValue, false, false, false);
    }

    protected StringProperty propString(final String name, final String initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final StringProperty p = new StringProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }

    protected IntegerProperty propInt(final String name, final Integer initialValue) {
        return propInt(name, initialValue, false, false, false);
    }

    protected IntegerProperty propInt(final String name, final Integer initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final IntegerProperty p = new IntegerProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }

    protected BooleanProperty propBool(final String name, final Boolean initialValue) {
        return propBool(name, initialValue, false, false, false);
    }

    protected BooleanProperty propBool(final String name, final Boolean initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final BooleanProperty p = new BooleanProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }

    protected LongProperty propLong(final String name, final Long initialValue) {
        return propLong(name, initialValue, false, false, false);
    }

    protected LongProperty propLong(final String name, final Long initialValue, final boolean defaultSentToClient, final boolean auditable, final boolean isIndexable) {
        final LongProperty p = new LongProperty(this, name, initialValue, defaultSentToClient, auditable, isIndexable);

        addPropertyMeta(name, p);

        return p;
    }
}
