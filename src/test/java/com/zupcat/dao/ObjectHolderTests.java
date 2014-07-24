package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.model.ObjectHolder;

public class ObjectHolderTests extends AbstractTest {


    /*
    RetryingExecutor
    Resource
     */

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSimpleCompressSerialization() {
        trySimpleSerialization(true);
    }

    public void testSimpleSerialization() {
        trySimpleSerialization(false);
    }

    private byte[] trySimpleSerialization(final boolean compress) {
        final ObjectHolder source = new ObjectHolder();
        source.getObjectVar().set("s", "s1");
        source.getObjectVar().set("b", true);
        source.getObjectVar().set("i", 100);
        source.getObjectVar().set("l", 123123123123123l);

        final ObjectHolder target = ObjectHolder.deserialize(ObjectHolder.deserialize(source.serialize(compress), compress).serialize(compress), compress);
        assertEquals(source.getObjectVar().getString("s"), target.getObjectVar().getString("s"));
        assertEquals(source.getObjectVar().getBoolean("b"), target.getObjectVar().getBoolean("b"));
        assertEquals(source.getObjectVar().getInteger("i"), target.getObjectVar().getInteger("i"));
        assertEquals(source.getObjectVar().getLong("l"), target.getObjectVar().getLong("l"));

        return target.serialize(compress);
    }
}
