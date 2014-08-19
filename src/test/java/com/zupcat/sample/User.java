package com.zupcat.sample;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.*;

public class User extends DatastoreEntity {

    public StringProperty FIRSTNAME;
    public StringProperty LASTNAME;
    public IntegerProperty AGE;
    public LongProperty LONG_VALUE;
    public BooleanProperty IS_FAKE;

    public ListStringProperty LIST_STRING;
    public ListIntegerProperty LIST_INT;
    public ListLongProperty LIST_LONG;

    public MapStringStringProperty MAP_STRING_STRING;
    public MapStringLongProperty MAP_STRING_LONG;
    public MapStringIntegerProperty MAP_STRING_INTEGER;
    public MapIntegerIntegerProperty MAP_INTEGER_INTEGER;

    public MapIntegerStringProperty INT_PER_STRING;
    public MapStringMapStringStringProperty MAP_STRING_MAP_STRING_STRING;

    public ObjectVarProperty<Address> ADDRESS;
    public ListObjectVarProperty<Address> ADDRESSES;


    public User() {
        super(CacheStrategy.SESSION_CACHE);
    }

    @Override
    protected void config() {
        FIRSTNAME = string();
        LASTNAME = string(null, false, false, true);

        AGE = integer();
        LONG_VALUE = longInt();
        IS_FAKE = bool();

        LIST_STRING = listString();
        LIST_INT = listInteger();
        LIST_LONG = listLong();

        MAP_STRING_STRING = mapStringString();
        MAP_STRING_LONG = mapStringLong();
        MAP_STRING_INTEGER = mapStringInteger();
        MAP_INTEGER_INTEGER = mapIntegerInteger();
        INT_PER_STRING = mapIntegerString();
        MAP_STRING_MAP_STRING_STRING = mapStringMapStringString();

        ADDRESS = objectVarProperty(Address.class);
        ADDRESSES = listObjectVarProperty(Address.class);
    }
}
