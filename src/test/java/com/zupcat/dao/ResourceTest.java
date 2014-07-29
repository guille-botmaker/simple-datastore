package com.zupcat.dao;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.zupcat.AbstractTest;
import com.zupcat.model.Resource;
import com.zupcat.util.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ResourceTest extends AbstractTest {

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[1][0]);
    }

    @Test
    public void testSavingAndLoadingJavaObject() {
        final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        helper.setUp();
        final String key = "testid-" + RandomUtils.getInstance().getRandomSafeString(20);

        Resource.buildJavaObject(key, testClass).save(false);
        final TestClass result = (TestClass) Resource.load(key).getJavaObject();

        assertEquals(result, testClass);
    }

    @Test
    public void testSimpleSerialization() {
        final byte[] source = new byte[]{35, 17, 0x00, 33, 0x24};
        final String key = "testid-" + RandomUtils.getInstance().getRandomSafeString(20);

        Resource.buildUnknownType(key, "ByteArray", source).save(false);
        final byte[] result = Resource.load(key).getRawValue();

        assertEquals(result.length, source.length);

        for (int i = 0; i < result.length; i++) {
            assertEquals(result[i], source[i]);
        }
    }
}
