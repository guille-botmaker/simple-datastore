package io.botmaker.simpleredis.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import io.botmaker.simpleredis.model.config.LONG;
import io.botmaker.simpleredis.model.config.PropertyMeta;
import io.botmaker.simpleredis.model.config.STRING;
import io.botmaker.simpleredis.property.ByteArrayProperty;
import io.botmaker.simpleredis.property.ListProperty;
import io.botmaker.simpleredis.property.LongProperty;
import io.botmaker.simpleredis.property.StringProperty;
import io.botmaker.simpleredis.util.RandomUtils;
import io.botmaker.simpleredis.util.TimeUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public abstract class RedisEntity extends PersistentObject implements Serializable {

    public static final int MAX_GROUPS = 100;

    protected static final int EXPIRING_1_HOUR = 60 * 60;
    protected static final int EXPIRING_1_DAY = EXPIRING_1_HOUR * 24;
    protected static final int EXPIRING_1_WEEK = EXPIRING_1_DAY * 7;
    protected static final int EXPIRING_1_MONTH = EXPIRING_1_WEEK * 4;
    protected static final int EXPIRING_6_MONTHS = EXPIRING_1_MONTH * 6;
    protected static final int EXPIRING_1_YEAR = EXPIRING_1_MONTH * 12;
    protected static final int EXPIRING_3_YEARS = EXPIRING_1_YEAR * 3;
    protected static final int EXPIRING_NEVER = 0;

    private static final long serialVersionUID = 6181606486836703354L;
    // persistent state
    private final DataObject dataObject = new DataObject();

    private final String entityName;
    private final int secondsToExpire; // 0 means never
    private final boolean usesAppIdPrefix;

    private final Map<String, PropertyMeta> propertiesMetadata = new HashMap<>();
    // entity usefull properties

//    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
//    public IntegerProperty GROUP_ID;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public LongProperty LAST_MODIFICATION;

    public StringProperty OBJECT_TYPE;


    protected RedisEntity(final boolean usesAppIdPrefix, final int secondsToExpire) {
        entityName = getEntityName(this.getClass());

        setNewId();

        this.secondsToExpire = secondsToExpire;
        this.usesAppIdPrefix = usesAppIdPrefix;

//        GROUP_ID = new INT(this).indexable().build();
        LAST_MODIFICATION = new LONG(this).sendToClient().mandatory().build();
        OBJECT_TYPE = new STRING(this).sendToClient().mandatory().build();

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

        setModified();

//        GROUP_ID.set(Math.abs(getId().hashCode() % MAX_GROUPS));
        OBJECT_TYPE.set(getEntityName());
    }

    public static String getEntityName(final Class<? extends RedisEntity> clazz) {
        final String className = clazz.getName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public int getSecondsToExpire() {
        return secondsToExpire;
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

    public boolean usesAppIdPrefix() {
        return usesAppIdPrefix;
    }

    @Override
    public void setModified() {
        LAST_MODIFICATION.set(TimeUtils.buildStandardModificationTime());
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getEntityName() {
        return entityName;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isFullyEquals(final RedisEntity other) {
        if (
                other == null ||
                        !Objects.equals(this.getId(), other.getId()) ||
                        !Objects.equals(this.entityName, other.entityName) ||
//                        !Objects.equals(this.GROUP_ID.get(), other.GROUP_ID.get()) ||
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

    private boolean comparePropertiesWith(final RedisEntity anotherRedisEntity) {
        if (anotherRedisEntity == null) {
            return false;
        }

        if (this.propertiesMetadata.size() != anotherRedisEntity.propertiesMetadata.size()) {
            return false;
        }

        if (this.propertiesMetadata.isEmpty()) {
            return true;
        }

        for (final Map.Entry<String, PropertyMeta> myEntries : this.propertiesMetadata.entrySet()) {
            final PropertyMeta anotherPropertyMeta = anotherRedisEntity.propertiesMetadata.get(myEntries.getKey());

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

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
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

    public PropertyMeta getIndexablePropertyByName(final String name) {
        final Optional<PropertyMeta> result = getIndexableProperties().stream().filter(p -> p.getPropertyName().equals(name)).findFirst();

        return result.isPresent() ? result.get() : null;
    }

    public List<PropertyMeta> getIndexableProperties() {
        return propertiesMetadata
                .values().stream()
                .filter(PropertyMeta::isIndexable)
                .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return getDataObject().optString(WithIdDataObject.ID_KEY, null);
    }

    @Override
    public void setId(final String id) {
        getDataObject().put(WithIdDataObject.ID_KEY, id);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Map<String, PropertyMeta> getPropertiesMetadata() {
        return propertiesMetadata;
    }

    public void addPropertyMeta(final String name, final PropertyMeta propertyMeta) {
//        if (propertyMeta.isIndexable() && propertyMeta.getInitialValue() == null) {
//            throw new RuntimeException("Property [" + name + "] of Entity [" + entityName + "] is indexable and has null default value. This is not allowed. Please change then initialValue to be not null");
//        }
        propertiesMetadata.put(name, propertyMeta);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public DataObject getDataObject() {
        return dataObject;
    }

    @Override
    public String toString() {
        return "[" + entityName + "|" + getId() + "|" + getDataObject().toString(5) + "]";
    }

    protected abstract void config();

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public int getDaysSinceLastModification() {
        return getMinutesSinceLastModification() / (60 * 24);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public int getMinutesSinceLastModification() {
        final long lm = LAST_MODIFICATION.get();

        if (lm == 0L) {
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

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Date getLastModificationAsDate() {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(Long.toString(LAST_MODIFICATION.get()));
        } catch (final ParseException _parseException) {
            throw new RuntimeException("Problems parsing date with value [" + LAST_MODIFICATION.get() + "]. Expected format [" + DATE_FORMAT + "]: " + _parseException.getMessage(), _parseException);
        }
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean hasAccessedToday() {
        final String lastAccessDate = Long.toString(LAST_MODIFICATION.get()).substring(0, 6);
        final String today = Long.toString(TimeUtils.buildStandardModificationTime()).substring(0, 6);

        return lastAccessDate.equals(today);
    }
}
