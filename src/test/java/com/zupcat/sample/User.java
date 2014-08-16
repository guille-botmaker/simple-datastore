package com.zupcat.sample;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.*;

public class User extends DatastoreEntity {

    public final StringProperty FIRSTNAME = string("fn", null);
    public final StringProperty LASTNAME = string("ln", null, false, false, true);
    public final IntegerProperty AGE = integer("ag", null);
    public final LongProperty LONG_VALUE = longInt("lv", 0l);
    public final BooleanProperty IS_FAKE = bool("f", false);

    public final ListStringProperty LIST_STRING = listString("ls", false, false);
    public final ListIntegerProperty LIST_INT = listInteger("li", false, false);
    public final ListLongProperty LIST_LONG = listLong("ll", false, false);

    public final MapStringStringProperty MESSAGES_MAP = mapStringString("mm", false, false);
    public final MapStringLongProperty MESSAGES_BIG_COUNTER = mapStringLong("mmbc", false, false);
    public final MapStringIntegerProperty MESSAGES_COUNTER = mapStringInteger("mmc", false, false);
    public final MapIntegerIntegerProperty QTY_PER_QTY = mapIntegerInteger("qpq", false, false);
    public final MapIntegerStringProperty INT_PER_STRING = mapIntegerString("ips", false, false);
    public final MapStringMapStringStringProperty MAP_STRING_MAP_STRING_STRING = mapStringMapStringString("msmss", false, false);

    public final ObjectVarProperty<Address> ADDRESS = new ObjectVarProperty<>(this, "ad", Address.class);
    public final ListObjectVarProperty<Address> ADDRESSES = new ListObjectVarProperty<>(this, "ads", Address.class);


    public User() {
        super(CacheStrategy.SESSION_CACHE);
    }
}
