package com.zupcat;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;
import com.zupcat.service.SimpleDatastoreService;
import com.zupcat.service.SimpleDatastoreServiceFactory;
import com.zupcat.util.RandomUtils;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTest extends TestCase {

    protected SimpleDatastoreService service;
    protected final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    protected TestClass testClass;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        service = SimpleDatastoreServiceFactory.getSimpleDatastoreService();
        service.registerDAO(new SampleUserDAO());

        helper.setUp();

        testClass = new TestClass();
        testClass.other = new TestClass();

        final RandomUtils randomUtils = RandomUtils.getInstance();

        testClass.s = randomUtils.getRandomSafeAlphaNumberString(5);
        testClass.i = randomUtils.getRandomInt(1000000);
        testClass.l = randomUtils.getRandomLong();

        testClass.other.s = randomUtils.getRandomSafeAlphaNumberString(5);
        testClass.other.i = randomUtils.getRandomInt(1000000);
        testClass.other.l = randomUtils.getRandomLong();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        helper.tearDown();
    }

    protected static List<SampleUser> buildUsers() {
        final int samples = 100;
        final List<SampleUser> result = new ArrayList<>(samples);
        final RandomUtils randomUtils = RandomUtils.getInstance();

        for (int i = 0; i < samples; i++) {
            final SampleUser sample = new SampleUser();
            sample.FIRSTNAME.set("First Name " + randomUtils.getRandomSafeString(10));
            sample.LASTNAME.set("LAST Name " + randomUtils.getRandomSafeString(10));
            sample.AGE.set(randomUtils.getIntBetweenInclusive(1, 100));
            sample.LONG_VALUE.set(randomUtils.getRandomLong());
            sample.IS_FAKE.set(randomUtils.getRandomBoolean());

            for (int j = 0; j < 10; j++) {
                sample.MESSAGES_MAP.put("" + j, randomUtils.getRandomSafeString(10));
                sample.MESSAGES_BIG_COUNTER.put("" + j, randomUtils.getRandomLong());
                sample.MESSAGES_COUNTER.put("" + j, randomUtils.getRandomInt(Integer.MAX_VALUE));
                sample.QTY_PER_QTY.put(j, randomUtils.getRandomInt(Integer.MAX_VALUE));
                sample.INT_PER_STRING.put(j, randomUtils.getRandomSafeString(10));
                sample.LIST_STRING.add(randomUtils.getRandomSafeString(10));
                sample.LIST_INT.add(randomUtils.getRandomInt(Integer.MAX_VALUE));
                sample.LIST_LONG.add(randomUtils.getRandomLong());
            }

            result.add(sample);
        }

        final SampleUser sample = new SampleUser();
        sample.FIRSTNAME.set("hernan");
        sample.LASTNAME.set("liendo");
        sample.AGE.set(18);
        sample.LONG_VALUE.set(23123213l);
        sample.IS_FAKE.set(false);

        result.add(sample);

        return result;
    }


    public static final class TestClass implements Serializable {

        private static final long serialVersionUID = 471847964351314234L;

        public String s;
        public int i;
        public long l;
        public TestClass other;

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TestClass testClass = (TestClass) o;

            return i == testClass.i && l == testClass.l && !(other != null ? !other.equals(testClass.other) : testClass.other != null) && !(s != null ? !s.equals(testClass.s) : testClass.s != null);
        }

        @Override
        public int hashCode() {
            int result = s != null ? s.hashCode() : 0;
            result = 31 * result + i;
            result = 31 * result + (int) (l ^ (l >>> 32));
            result = 31 * result + (other != null ? other.hashCode() : 0);
            return result;
        }
    }
}
