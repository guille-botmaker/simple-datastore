package com.zupcat.dao;

import com.zupcat.AbstractTest;

import java.io.File;

public class SerializationHelperTests extends AbstractTest {


    public void testSerialization() {
        final TestClass result = (TestClass) SerializationHelper.getObjectFromCompressedBytes(
                SerializationHelper.getCompressedBytes(
                        SerializationHelper.getObjectFromCompressedBytes(
                                SerializationHelper.getCompressedBytes(testClass)
                        )
                )
        );

        assertEquals(result, testClass);
    }

    public void testFileSerialization() throws Exception {
        File file = null;
        TestClass result = testClass;

        try {
            file = File.createTempFile(this.getClass().getName(), "dat");

            SerializationHelper.saveObject2File(result, file);
            result = (TestClass) SerializationHelper.loadObjectFromFile(file);

            SerializationHelper.saveObject2File(result, file);
            result = (TestClass) SerializationHelper.loadObjectFromFile(file);

            SerializationHelper.saveObject2File(result, file);
            result = (TestClass) SerializationHelper.loadObjectFromFile(file);

            assertEquals(result, testClass);
        } finally {
            if (file != null) {
                file.deleteOnExit();
            }
        }
    }
}
