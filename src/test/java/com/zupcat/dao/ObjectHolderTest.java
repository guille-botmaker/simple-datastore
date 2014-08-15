package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.model.AvroSerializer;
import com.zupcat.model.ObjectHolder;
import com.zupcat.model.ObjectVar;
import com.zupcat.util.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ObjectHolderTest extends AbstractTest {

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[2][0]);
    }

    @Test
    public void testObjectVarWithObjectVars() {
        final ObjectVar sourceInnerObjectVar = new ObjectVar();
        sourceInnerObjectVar.set("str", "an inner str");
        sourceInnerObjectVar.set("int", "an inner int");

        final ObjectHolder sourceObjectHolder = new ObjectHolder();
        sourceObjectHolder.getObjectVar().set("str", "a string");
        sourceObjectHolder.getObjectVar().set("int", "an int");
        sourceObjectHolder.getObjectVar().set("aov", sourceInnerObjectVar);

        final AvroSerializer<ObjectHolder> objectHolderAvroSerializer = new AvroSerializer<>();

        final ObjectHolder targetObjectHolder =
                objectHolderAvroSerializer.deserialize(
                        objectHolderAvroSerializer.serialize(
                                objectHolderAvroSerializer.deserialize(
                                        objectHolderAvroSerializer.serialize(sourceObjectHolder, ObjectHolder.class, true),
                                        ObjectHolder.class,
                                        true
                                )
                                , ObjectHolder.class, true)

                        , ObjectHolder.class, true);

        Assert.assertTrue(sourceObjectHolder.isFullyEquals(targetObjectHolder));

        Assert.assertEquals(targetObjectHolder.getObjectVar().getObjectVar("aov").getString("str"), "an inner str");
    }


    @Test
    public void testSimpleCompressSerialization() {
        trySimpleSerialization(true);
    }

    @Test
    public void testSimpleSerialization() {
        trySimpleSerialization(false);
    }

    private void trySimpleSerialization(final boolean compress) {
        for (int i = 0; i < 10; i++) {
            final ObjectHolder source = build();

            final AvroSerializer<ObjectHolder> objectHolderAvroSerializer = new AvroSerializer<>();

            final ObjectHolder target =
                    objectHolderAvroSerializer.deserialize(
                            objectHolderAvroSerializer.serialize(
                                    objectHolderAvroSerializer.deserialize(
                                            objectHolderAvroSerializer.serialize(source, ObjectHolder.class, compress),
                                            ObjectHolder.class,
                                            compress
                                    )
                                    , ObjectHolder.class, compress)

                            , ObjectHolder.class, compress);


            assertTrue(source.isFullyEquals(target));
            assertTrue(target.isFullyEquals(source));
            assertTrue(source.isFullyEquals(source));
            assertTrue(target.isFullyEquals(target));
            assertEquals(source.toString(), target.toString());

//            final int size = source.serialize(compress).length;
//            System.err.println("");
        }
    }

    private ObjectHolder build() {
        final ObjectHolder objectHolder = new ObjectHolder();

        fillObjectVar(objectHolder.getObjectVar());

        for (int i = 0; i < 1000; i++) {
            final ObjectVar item = new ObjectVar();
            fillObjectVar(item);

            objectHolder.addItem(item);
        }
        return objectHolder;
    }

    private void fillObjectVar(final ObjectVar objectVar) {
        final RandomUtils randomUtils = RandomUtils.getInstance();

        objectVar.set("string", randomUtils.getRandomSafeAlphaNumberString(20));
        objectVar.set("long", randomUtils.getRandomLong());
        objectVar.set("bool", randomUtils.getRandomBoolean());
        objectVar.set("int", randomUtils.getRandomInt(Integer.MAX_VALUE));
    }
}
