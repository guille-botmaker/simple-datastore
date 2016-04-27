package io.botmaker.tests.sample;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.model.config.*;
import io.botmaker.simpleredis.property.*;

import java.util.Map;

public class User extends RedisEntity {

    public StringProperty FIRSTNAME;
    public StringProperty LASTNAME;
    public IntegerProperty AGE;
    public ByteArrayProperty BYTES;
    public LongProperty LONG_VALUE;
    public BooleanProperty IS_FAKE;

    public ListProperty<String> LIST_STRING;
    public ListProperty<Integer> LIST_INT;
    public ListProperty<Long> LIST_LONG;

    public MapProperty<String> MAP_STRING_STRING;
    public MapProperty<Long> MAP_STRING_LONG;
    public MapProperty<Integer> MAP_STRING_INTEGER;

    public DataObjectProperty<Address> ADDRESS;
    public ListProperty<Address> ADDRESSES;
    public MapProperty<Address> ADDRESSES_MAP;
    public ComplexAnyProperty<Map<String, String>> COMPLEX_MAP_STRING_STRING; // slow implementation but supports anything serializable


    public User() {
        super(true, EXPIRING_1_HOUR);
    }

    @Override
    protected void config() {
        FIRSTNAME = new STRING(this).build();
        LASTNAME = new STRING(this).indexable().build();

        AGE = new INT(this).build();
        BYTES = new BYTE_ARRAY(this).build();
        LONG_VALUE = new LONG(this).build();
        IS_FAKE = new BOOL(this).build();

        LIST_STRING = new LIST<String>(this).build();
        LIST_INT = new LIST<Integer>(this).build();
        LIST_LONG = new LIST<Long>(this).build();

        MAP_STRING_STRING = new MAP<String>(this).build();
        MAP_STRING_LONG = new MAP<Long>(this).build();
        MAP_STRING_INTEGER = new MAP<Integer>(this).build();

        ADDRESS = new DATA_OBJECT<>(this, Address.class).build();
        ADDRESSES = new LIST<Address>(this, Address.class).build();
        ADDRESSES_MAP = new MAP<Address>(this, Address.class).build();
        COMPLEX_MAP_STRING_STRING = new COMPLEX_ANY<>(this).build();
    }
}
