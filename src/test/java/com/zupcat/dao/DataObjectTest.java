package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.model.DataObject;
import com.zupcat.model.DataObjectSerializer;
import com.zupcat.util.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class DataObjectTest extends AbstractTest {

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[2][0]);
    }

    @Test
    public void testDataObjectWithDataObjects() {
        final DataObject sourceInnerDataObject = new DataObject();
        sourceInnerDataObject.put("str", "an inner str");
        sourceInnerDataObject.put("int", "an inner int");

        final DataObject innerDataObject = new DataObject();
        innerDataObject.setDataObject(sourceInnerDataObject);

        final DataObject sourceDataObject = new DataObject();
        sourceDataObject.getDataObject().set("str", "a string");
        sourceDataObject.getDataObject().set("int", "an int");
        sourceDataObject.getDataObject().set("aov", innerDataObject);

        final DataObjectSerializer<DataObject> DataObjectAvroSerializer = new DataObjectSerializer<>();

        final DataObject targetDataObject =
                DataObjectAvroSerializer.deserialize(
                        DataObjectAvroSerializer.serialize(
                                DataObjectAvroSerializer.deserialize(
                                        DataObjectAvroSerializer.serialize(sourceDataObject, true),
                                        DataObject.class,
                                        true
                                )
                                , DataObject.class, true)

                        , DataObject.class, true);

        Assert.assertTrue(sourceDataObject.isFullyEquals(targetDataObject));

        Assert.assertEquals(targetDataObject.getDataObject().getDataObject("aov").getDataObject().getString("str"), "an inner str");
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
            final DataObject source = build();

            final DataObjectSerializer<DataObject> DataObjectAvroSerializer = new DataObjectSerializer<>();

            final DataObject target =
                    DataObjectAvroSerializer.deserialize(
                            DataObjectAvroSerializer.serialize(
                                    DataObjectAvroSerializer.deserialize(
                                            DataObjectAvroSerializer.serialize(source, DataObject.class, compress),
                                            DataObject.class,
                                            compress
                                    )
                                    , DataObject.class, compress)

                            , DataObject.class, compress);


            assertTrue(source.isFullyEquals(target));
            assertTrue(target.isFullyEquals(source));
            assertTrue(source.isFullyEquals(source));
            assertTrue(target.isFullyEquals(target));
            assertEquals(source.toString(), target.toString());

//            final int size = source.serialize(compress).length;
//            System.err.println("");
        }
    }

    private DataObject build() {
        final DataObject DataObject = new DataObject();

        fillDataObject(DataObject.getDataObject());

        for (int i = 0; i < 1000; i++) {
            final DataObject item = new DataObject();
            fillDataObject(item);

            DataObject.addItem(item);
        }
        return DataObject;
    }

    private void fillDataObject(final DataObject DataObject) {
        final RandomUtils randomUtils = RandomUtils.getInstance();

        DataObject.set("string", randomUtils.getRandomSafeAlphaNumberString(20));
        DataObject.set("long", randomUtils.getRandomLong());
        DataObject.set("bool", randomUtils.getRandomBoolean());
        DataObject.set("int", randomUtils.getRandomInt(Integer.MAX_VALUE));
    }
}
