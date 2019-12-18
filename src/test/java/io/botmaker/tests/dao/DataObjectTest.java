package io.botmaker.tests.dao;

import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.DataObjectSerializer;
import io.botmaker.simpleredis.util.RandomUtils;
import io.botmaker.tests.AbstractTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(Parameterized.class)
public class DataObjectTest extends AbstractTest {

    private final DataObjectSerializer<DataObject> dataObjectAvroSerializer = new DataObjectSerializer<>();
    private boolean compress;

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[2][0]);
    }

    @Test
    public void testConcurrentModificationExceptionWhileCopyingDataObject() throws InterruptedException {

        final DataObject dataObject = new DataObject();
        dataObject.put("1", "1");
        dataObject.put("2", "2");
        dataObject.put("3", "3");

        final AtomicReference<RuntimeException> error = new AtomicReference<>();
        final List<Thread> threads = new ArrayList<>();
        for (int index = 0; index < 5000; index++) {

            final Thread thread = new Thread(() -> {
                try {
                    dataObject.put("4", "4");
                    dataObject.remove("4");

                    // NOTE this should not throw ConcurrentModificationException
                    final JSONObject copy = new JSONObject(dataObject.getInternalMap());
                    System.err.println(copy);
                } catch (final Exception e) {
                    error.set(new RuntimeException(e));
                }
            });
            threads.add(thread);
            thread.start();
        }

        while (error.get() == null && threads.stream().anyMatch(t -> t.isAlive() && !t.isInterrupted())) {
            Thread.sleep(100);
        }

        if (error.get() != null)
            throw error.get();
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

            dataObject.addChild(item);
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
