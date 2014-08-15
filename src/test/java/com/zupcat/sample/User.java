package com.zupcat.sample;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.*;

public class User extends DatastoreEntity {

    public final StringProperty FIRSTNAME;
    public final StringProperty LASTNAME;
    public final IntegerProperty AGE;
    public final LongProperty LONG_VALUE;
    public final BooleanProperty IS_FAKE;
    public final MapStringStringProperty MESSAGES_MAP;
    public final MapStringLongProperty MESSAGES_BIG_COUNTER;
    public final MapStringIntegerProperty MESSAGES_COUNTER;
    public final MapIntegerIntegerProperty QTY_PER_QTY;
    public final MapIntegerStringProperty INT_PER_STRING;
    public final ListStringProperty LIST_STRING;
    public final ListIntegerProperty LIST_INT;
    public final ListLongProperty LIST_LONG;
    public final MapStringMapStringStringProperty MAP_STRING_MAP_STRING_STRING;
    public final ObjectVarProperty<Address> ADDRESS;


    public User() {
        super(CacheStrategy.SESSION_CACHE);

        FIRSTNAME = propString("fn", null, false, false, false);
        LASTNAME = propString("ln", null, false, true, true);
        AGE = propInt("ag", null);
        LONG_VALUE = propLong("lv", 0l);
        IS_FAKE = propBool("f", false);
        MESSAGES_MAP = propMapStringString("mm", false, false);
        MESSAGES_BIG_COUNTER = propMapStringLong("mmbc", false, false);
        MESSAGES_COUNTER = propMapStringInteger("mmc", false, false);
        QTY_PER_QTY = propMapIntegerInteger("qpq", false, false);
        INT_PER_STRING = propMapIntegerString("ips", false, false);
        LIST_STRING = propListString("ls", false, false);
        LIST_INT = propListInteger("li", false, false);
        LIST_LONG = propListLong("ll", false, false);
        MAP_STRING_MAP_STRING_STRING = propMapStringMapStringString("msmss", false, false);
        ADDRESS = new ObjectVarProperty<>(this, "ad", Address.class);
    }
}
