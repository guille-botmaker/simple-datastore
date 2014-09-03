package com.zupcat.model;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.property.*;
import com.zupcat.util.TimeUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class DatastoreEntity extends PersistentObject implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    public static final int MAX_GROUPS = 100;

    // persistent state
    private final ObjectHolder objectHolder = new ObjectHolder();

    private final String entityName;
    private final CacheStrategy cacheStrategy;
    private final Map<String, PropertyMeta> propertiesMetadata = new HashMap<>();

    // entity usefull properties
    public IntegerProperty GROUP_ID;
    public LongProperty LAST_MODIFICATION;


    protected DatastoreEntity(final CacheStrategy cacheStrategy) {
        GROUP_ID = integer(null, false, false, true);
        LAST_MODIFICATION = longInt(null, false, false, true);

        this.cacheStrategy = cacheStrategy;

        final Class<? extends DatastoreEntity> clazz = this.getClass();
        final String className = clazz.getName();
        entityName = className.substring(className.lastIndexOf(".") + 1);

        config();

        for (final Field field : clazz.getFields()) {
            if (PropertyMeta.class.isAssignableFrom(field.getType())) {
                final String propertyName = field.getName();

                try {
                    final PropertyMeta propertyMeta = (PropertyMeta) field.get(this);

                    propertyMeta.setPropertyName(propertyName);
                    addPropertyMeta(propertyName, propertyMeta);

                } catch (final IllegalAccessException _illegalAccessException) {
                    throw new RuntimeException("Problems getting value for field [" + propertyName + "], of class [" + clazz + "]. Possible private variable?: " + _illegalAccessException.getMessage(), _illegalAccessException);
                }
            }
        }
        GROUP_ID.set(Math.abs(getId().hashCode() % MAX_GROUPS));
        setModified();
    }

    @Override
    public void setModified() {
        LAST_MODIFICATION.set(TimeUtils.buildStandardModificationTime());
    }

    public String getEntityName() {
        return entityName;
    }

    public boolean isFullyEquals(final DatastoreEntity other) {
        if (
                other == null ||
                        !Objects.equals(this.getId(), other.getId()) ||
                        !Objects.equals(this.cacheStrategy, other.cacheStrategy) ||
                        !Objects.equals(this.entityName, other.entityName) ||
                        !Objects.equals(this.GROUP_ID.get(), other.GROUP_ID.get()) ||
                        !Objects.equals(this.LAST_MODIFICATION.get(), other.LAST_MODIFICATION.get()) ||
                        this.propertiesMetadata.size() != other.propertiesMetadata.size() ||
                        !this.objectHolder.isFullyEquals(other.objectHolder)
                ) {
            return false;
        }

        for (final Map.Entry<String, PropertyMeta> entry : this.propertiesMetadata.entrySet()) {
            if (!entry.getValue().isFullyEquals(other.propertiesMetadata.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public Map<String, PropertyMeta> getPropertiesMetadata() {
        return propertiesMetadata;
    }

    public void addPropertyMeta(final String name, final PropertyMeta propertyMeta) {
//        if (propertyMeta.isIndexable() && propertyMeta.getInitialValue() == null) {
//            throw new RuntimeException("Property [" + name + "] of Entity [" + entityName + "] is indexable and has null default value. This is not allowed. Please change then initialValue to be not null");
//        }
        propertiesMetadata.put(name, propertyMeta);
    }

    /**
     * For framework internal calls. Do not use this method directly
     */
    public ObjectHolder getInternalObjectHolder() {
        return objectHolder;
    }

    public ObjectHolder getObjectHolder() {
        // commiting changes
        for (final PropertyMeta propertyMeta : getPropertiesMetadata().values()) {
            propertyMeta.commit();
        }
        return objectHolder;
    }

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(500);

        builder
                .append("[")
                .append(entityName)
                .append("|")
                .append(getId())
                .append("|")
                .append(objectHolder.toString(builder));

        builder.append("]");

        return builder.toString();
    }

    protected abstract void config();


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

    protected void setObjectVarType(final String type) {
        getInternalObjectHolder().getObjectVar().set("_t", type);
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

    protected StringProperty string() {
        return string(null, false, false, false);
    }

    protected StringProperty string(final String initialValue, final boolean sentToClient, final boolean auditable, final boolean isIndexable) {
        return new StringProperty(this, initialValue, sentToClient, auditable, isIndexable);
    }

    protected IntegerProperty integer() {
        return integer(0, false, false, false);
    }

    protected IntegerProperty integer(final Integer initialValue, final boolean sentToClient, final boolean auditable, final boolean isIndexable) {
        return new IntegerProperty(this, initialValue, sentToClient, auditable, isIndexable);
    }

    protected BooleanProperty bool() {
        return bool(false, false, false, false);
    }

    protected BooleanProperty bool(final Boolean initialValue, final boolean sentToClient, final boolean auditable, final boolean isIndexable) {
        return new BooleanProperty(this, initialValue, sentToClient, auditable, isIndexable);
    }

    protected LongProperty longInt() {
        return longInt(0l, false, false, false);
    }

    protected LongProperty longInt(final Long initialValue, final boolean sentToClient, final boolean auditable, final boolean isIndexable) {
        return new LongProperty(this, initialValue, sentToClient, auditable, isIndexable);
    }

    protected ListIntegerProperty listInteger() {
        return listInteger(false, false);
    }

    protected ListIntegerProperty listInteger(final boolean sentToClient, final boolean auditable) {
        return new ListIntegerProperty(this, sentToClient, auditable);
    }

    protected ListLongProperty listLong() {
        return listLong(false, false);
    }

    protected ListLongProperty listLong(final boolean sentToClient, final boolean auditable) {
        return new ListLongProperty(this, sentToClient, auditable);
    }

    protected ListStringProperty listString() {
        return listString(false, false);
    }

    protected ListStringProperty listString(final boolean sentToClient, final boolean auditable) {
        return new ListStringProperty(this, sentToClient, auditable);
    }

    protected MapStringStringProperty mapStringString() {
        return mapStringString(false, false);
    }

    protected MapStringStringProperty mapStringString(final boolean sentToClient, final boolean auditable) {
        return new MapStringStringProperty(this, sentToClient, auditable);
    }

    protected MapStringIntegerProperty mapStringInteger() {
        return mapStringInteger(false, false);
    }

    protected MapStringIntegerProperty mapStringInteger(final boolean sentToClient, final boolean auditable) {
        return new MapStringIntegerProperty(this, sentToClient, auditable);
    }

    protected MapIntegerIntegerProperty mapIntegerInteger() {
        return mapIntegerInteger(false, false);
    }

    protected MapIntegerIntegerProperty mapIntegerInteger(final boolean sentToClient, final boolean auditable) {
        return new MapIntegerIntegerProperty(this, sentToClient, auditable);
    }

    protected MapIntegerStringProperty mapIntegerString() {
        return mapIntegerString(false, false);
    }

    protected MapIntegerStringProperty mapIntegerString(final boolean sentToClient, final boolean auditable) {
        return new MapIntegerStringProperty(this, sentToClient, auditable);
    }

    protected MapStringLongProperty mapStringLong() {
        return mapStringLong(false, false);
    }

    protected MapStringLongProperty mapStringLong(final boolean sentToClient, final boolean auditable) {
        return new MapStringLongProperty(this, sentToClient, auditable);
    }


    protected MapStringMapStringIntegerProperty mapStringMapStringInteger() {
        return mapStringMapStringInteger(false, false);
    }

    protected MapStringMapStringIntegerProperty mapStringMapStringInteger(final boolean sentToClient, final boolean auditable) {
        return new MapStringMapStringIntegerProperty(this, sentToClient, auditable);
    }

    protected MapStringMapStringStringProperty mapStringMapStringString() {
        return mapStringMapStringString(false, false);
    }

    protected MapStringMapStringStringProperty mapStringMapStringString(final boolean sentToClient, final boolean auditable) {
        return new MapStringMapStringStringProperty(this, sentToClient, auditable);
    }

    protected ObjectVarProperty objectVarProperty(final Class clazz) {
        return objectVarProperty(clazz, false, false);
    }

    protected ObjectVarProperty objectVarProperty(final Class clazz, final boolean sentToClient, final boolean auditable) {
        return new ObjectVarProperty(this, clazz, sentToClient, auditable);
    }

    protected ListObjectVarProperty listObjectVarProperty(final Class clazz) {
        return listObjectVarProperty(clazz, false, false);
    }

    protected ListObjectVarProperty listObjectVarProperty(final Class clazz, final boolean sentToClient, final boolean auditable) {
        return new ListObjectVarProperty(this, clazz, sentToClient, auditable);
    }

    protected MapStringObjectVarProperty mapStringObjectVarProperty(final Class clazz) {
        return mapStringObjectVarProperty(clazz, false, false);
    }

    protected MapStringObjectVarProperty mapStringObjectVarProperty(final Class clazz, final boolean sentToClient, final boolean auditable) {
        return new MapStringObjectVarProperty(this, clazz, sentToClient, auditable);
    }
}
