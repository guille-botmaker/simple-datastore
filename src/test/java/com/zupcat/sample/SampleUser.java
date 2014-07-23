package com.zupcat.sample;

import com.zupcat.cache.CacheStrategy;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.property.IntegerProperty;
import com.zupcat.property.StringProperty;

public class SampleUser extends DatastoreEntity {

    public final IntegerProperty AGE;
    public final StringProperty NAME;


    public SampleUser() {
        super(CacheStrategy.SESSION_CACHE);

        NAME = propString("n", null);
        AGE = propInt("ag", null);
    }
}
