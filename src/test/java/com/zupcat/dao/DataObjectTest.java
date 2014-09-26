package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.model.DataObject;
import com.zupcat.model.DataObjectSerializer;
import com.zupcat.util.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class DataObjectTest extends AbstractTest {

    private final DataObjectSerializer<DataObject> dataObjectAvroSerializer = new DataObjectSerializer<>();
    private boolean compress;

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[2][0]);
    }

    @Test
    public void testDataObjectWithDataObjects() {
        final DataObject sourceInnerDataObject = new DataObject();
        sourceInnerDataObject.put("str", "an inner str");
        sourceInnerDataObject.put("int", "an inner int");

        final DataObject sourceDataObject = new DataObject();
        sourceDataObject.put("str", "a string");
        sourceDataObject.put("int", "an int");
        sourceDataObject.put("aov", sourceInnerDataObject);

        compress = true;

        final DataObject targetDataObject = des(ser(des(ser(sourceDataObject))));

        Assert.assertTrue(sourceDataObject.isFullyEquals(targetDataObject));

        Assert.assertEquals(targetDataObject.getJSONObject("aov").getString("str"), "an inner str");
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
        this.compress = compress;

        for (int i = 0; i < 10; i++) {
            final DataObject source = build();

            final DataObject target = des(ser(des(ser(source))));

            Assert.assertTrue(source.isFullyEquals(target));
            Assert.assertTrue(target.isFullyEquals(source));
            Assert.assertTrue(source.isFullyEquals(source));
            Assert.assertTrue(target.isFullyEquals(target));
            Assert.assertEquals(source.toString(), target.toString());
        }
    }

    private byte[] ser(final DataObject dataObject) {
        return dataObjectAvroSerializer.serialize(dataObject, compress);
    }

    private DataObject des(final byte[] bytes) {
        final DataObject result = new DataObject();

        dataObjectAvroSerializer.deserialize(bytes, result, compress);

        return result;
    }

    private DataObject build() {
        final DataObject dataObject = new DataObject();

        fillDataObject(dataObject);

        for (int i = 0; i < 1000; i++) {
            final DataObject item = new DataObject();
            fillDataObject(item);

            dataObject.addItem(item);
        }
        return dataObject;
    }

    private void fillDataObject(final DataObject dataObject) {
        final RandomUtils randomUtils = RandomUtils.getInstance();

        dataObject.put("string", randomUtils.getRandomSafeAlphaNumberString(20));
        dataObject.put("long", randomUtils.getRandomLong());
        dataObject.put("bool", randomUtils.getRandomBoolean());
        dataObject.put("int", randomUtils.getRandomInt(Integer.MAX_VALUE));

        final DataObject another = new DataObject();
        another.put("string", randomUtils.getRandomSafeAlphaNumberString(20));

        dataObject.put("anotherJSON", another);
    }
}
