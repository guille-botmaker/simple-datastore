package com.zupcat.sample;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.*;

public class SampleUser extends DatastoreEntity {

    public final StringProperty FIRSTNAME;
    public final StringProperty LASTNAME;
    public final IntegerProperty AGE;
    public final LongProperty LONG_VALUE;
    public final BooleanProperty IS_FAKE;
    public final MapStringStringProperty MESSAGES_MAP;


    public SampleUser() {
        super(CacheStrategy.SESSION_CACHE);

        FIRSTNAME = propString("fn", null, false, false, false);
        LASTNAME = propString("ln", null, false, true, true);
        AGE = propInt("ag", null);
        LONG_VALUE = propLong("lv", 0l);
        IS_FAKE = propBool("f", false);
        MESSAGES_MAP = new MapStringStringProperty(this, "mm", false, false);
    }
}
