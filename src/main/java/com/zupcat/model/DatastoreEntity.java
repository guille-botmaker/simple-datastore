package com.zupcat.model;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.config.INT;
import com.zupcat.model.config.LONG;
import com.zupcat.model.config.PropertyMeta;
import com.zupcat.property.IntegerProperty;
import com.zupcat.property.LongProperty;
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
    private final DataObject dataObject = new DataObject();

    private final String entityName;
    private final CacheStrategy cacheStrategy;
    private final Map<String, PropertyMeta> propertiesMetadata = new HashMap<>();

    // entity usefull properties
    public IntegerProperty GROUP_ID;
    public LongProperty LAST_MODIFICATION;


    protected DatastoreEntity(final CacheStrategy cacheStrategy) {
        GROUP_ID = new INT(this).indexable().build();
        LAST_MODIFICATION = new LONG(this).indexable().build();

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

    public boolean shouldBeSentToClient() {
        for (final PropertyMeta propertyMeta : propertiesMetadata.values()) {
            if (propertyMeta.hasToSendToClient()) {
                return true;
            }
        }
        return false;
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
                        !this.dataObject.isFullyEquals(other.dataObject)
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

    public DataObject getObjectHolderForClient() {
        final DataObject result = new DataObject();
        final DataObject source = getDataObject();

        result.mergeWith(source);

        for (final Map.Entry<String, PropertyMeta> entry : getPropertiesMetadata().entrySet()) {
            if (!entry.getValue().hasToSendToClient()) {
                result.remove(entry.getKey());
            }
        }
        return result;
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

    public DataObject getDataObject() {
        return dataObject;
    }

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    @Override
    public String toString() {
        return "[" + entityName + "|" + getId() + "|" + getDataObject().toString(5) + "]";
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
        getDataObject().put("_t", type);
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
}
