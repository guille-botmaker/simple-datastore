package com.zupcat.sample;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.config.*;
import com.zupcat.property.*;

public class User extends DatastoreEntity {

    public StringProperty FIRSTNAME;
    public StringProperty LASTNAME;
    public IntegerProperty AGE;
    public ByteArrayProperty BYTES;
    public LongProperty LONG_VALUE;
    public BooleanProperty IS_FAKE;

    public ListProperty<String> LIST_STRING;
    public ListIntegerProperty LIST_INT;
    public ListLongProperty LIST_LONG;

    public MapStringStringProperty MAP_STRING_STRING;
    public MapStringLongProperty MAP_STRING_LONG;
    public MapStringIntegerProperty MAP_STRING_INTEGER;
    public MapIntegerIntegerProperty MAP_INTEGER_INTEGER;

    public MapIntegerStringProperty INT_PER_STRING;
    public MapStringMapStringStringProperty MAP_STRING_MAP_STRING_STRING;

    public ObjectVarProperty<Address> ADDRESS;
    public ListProperty<Address> ADDRESSES;


    public User() {
        super(CacheStrategy.SESSION_CACHE);
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
        LIST_INT = new LIST_INTEGER(this).build();
        LIST_LONG = new LIST_LONG(this).build();

        MAP_STRING_STRING = new MAP_STRING_STRING(this).build();
        MAP_STRING_LONG = new MAP_STRING_LONG(this).build();
        MAP_STRING_INTEGER = new MAP_STRING_INTEGER(this).build();
        MAP_INTEGER_INTEGER = new MAP(this).build();
        INT_PER_STRING = new MAP_INT_STRING(this).build();
        MAP_STRING_MAP_STRING_STRING = new MAP_STRING_MAP_STRING_STRING(this).build();

        ADDRESS = new OBJECT_VAR<>(this, Address.class).build();
        ADDRESSES = new LIST<Address>(this).build();
    }
}
