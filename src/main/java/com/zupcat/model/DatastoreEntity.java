package com.zupcat.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.config.INT;
import com.zupcat.model.config.LONG;
import com.zupcat.model.config.PropertyMeta;
import com.zupcat.property.ByteArrayProperty;
import com.zupcat.property.IntegerProperty;
import com.zupcat.property.ListProperty;
import com.zupcat.property.LongProperty;
import com.zupcat.util.RandomUtils;
import com.zupcat.util.TimeUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class DatastoreEntity extends PersistentObject implements Serializable {

    public static final int MAX_GROUPS = 100;
    private static final long serialVersionUID = 6181606486836703354L;
    // persistent state
    private final DataObject dataObject = new DataObject();

    private final String entityName;
    private final Map<String, PropertyMeta> propertiesMetadata = new HashMap<>();
    // entity usefull properties
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public IntegerProperty GROUP_ID;
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public LongProperty LAST_MODIFICATION;
    private CacheStrategy cacheStrategy;


    protected DatastoreEntity() {
        final Class<? extends DatastoreEntity> clazz = this.getClass();
        final String className = clazz.getName();
        entityName = className.substring(className.lastIndexOf(".") + 1);
        this.cacheStrategy = CacheStrategy.NO_CACHE;

        setNewId();
    }

    protected DatastoreEntity(final CacheStrategy cacheStrategy) {
        this();

        this.cacheStrategy = cacheStrategy;
        GROUP_ID = new INT(this).indexable().build();
        LAST_MODIFICATION = new LONG(this).sendToClient().mandatory().indexable().build();

        config();

        for (final Field field : getClass().getFields()) {
            if (PropertyMeta.class.isAssignableFrom(field.getType())) {
                final String propertyName = field.getName();

                try {
                    final PropertyMeta propertyMeta = (PropertyMeta) field.get(this);

                    propertyMeta.setPropertyName(propertyName);
                    addPropertyMeta(propertyName, propertyMeta);

                } catch (final IllegalAccessException _illegalAccessException) {
                    throw new RuntimeException("Problems getting value for field [" + propertyName + "], of class [" + getClass() + "]. Possible private variable?: " + _illegalAccessException.getMessage(), _illegalAccessException);
                }
            }
        }
        GROUP_ID.set(Math.abs(getId().hashCode() % MAX_GROUPS));
        setModified();
    }

    public void setNewId() {
        setId(RandomUtils.getInstance().getRandomSafeAlphaNumberString(20));
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
                        !comparePropertiesWith(other)
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

    private boolean comparePropertiesWith(final DatastoreEntity anotherDatastoreEntity) {
        if (anotherDatastoreEntity == null) {
            return false;
        }

        if (this.propertiesMetadata.size() != anotherDatastoreEntity.propertiesMetadata.size()) {
            return false;
        }

        if (this.propertiesMetadata.isEmpty()) {
            return true;
        }

        for (final Map.Entry<String, PropertyMeta> myEntries : this.propertiesMetadata.entrySet()) {
            final PropertyMeta anotherPropertyMeta = anotherDatastoreEntity.propertiesMetadata.get(myEntries.getKey());

            if (anotherPropertyMeta == null) {
                return false;
            }

            final PropertyMeta object1Property = myEntries.getValue();
            final Object object1 = object1Property.get();
            final Object object2 = anotherPropertyMeta.get();

            if (object1Property instanceof ByteArrayProperty) {
                if (!ArrayUtils.isEquals(object1, object2)) {
                    return false;
                }
            } else if (object1Property instanceof ListProperty) {
                if (!ListUtils.isEqualList((Collection<?>) object1, (Collection<?>) object2)) {
                    return false;
                }
            } else {
                if (!Objects.equals(object1, object2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public DataObject getDataObjectForClient() {
        final DataObject result = new DataObject();
        final DataObject source = getDataObject();

        result.mergeWith(source);

        for (final Map.Entry<String, PropertyMeta> entry : getPropertiesMetadata().entrySet()) {
            final PropertyMeta propertyMeta = entry.getValue();

            if (!propertyMeta.hasToSendToClient()) {
                result.remove(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public String getId() {
        return getDataObject().optString(WithIdDataObject.ID_KEY, null);
    }

    @Override
    public void setId(final String id) {
        getDataObject().put(WithIdDataObject.ID_KEY, id);
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
        now.setTimeZone(TimeZone.getTimeZone("GMT"));
        final GregorianCalendar modified = new GregorianCalendar();
        modified.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            now.setTime(dateFormat.parse(Long.toString(TimeUtils.buildStandardModificationTime())));
            modified.setTime(dateFormat.parse(Long.toString(lm)));

        } catch (final ParseException _parseException) {
            throw new RuntimeException("Problems when getting minutesSinceLastModification with value [" + lm + "]. Expected format [" + DATE_FORMAT + "]: " + _parseException.getMessage(), _parseException);
        }

        return ((int) (Math.abs((now.getTime().getTime() - modified.getTime().getTime())) / 60000));
    }

    protected void setDataObjectType(final String type) {
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
