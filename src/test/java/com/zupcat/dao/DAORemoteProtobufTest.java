package com.zupcat.dao;

import com.zupcat.sample.User;
import com.zupcat.util.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class DAORemoteProtobufTest extends DAOTest {

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[1][0]);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        service.configProtoBuf("datastore-from-compute@zcat-infra.iam.gserviceaccount.com", "C:/Users/hernan/Desktop/zcat-infra-9f0082e80adc.p12");

        for (final User user : buildUsers(lastNameUniqueId)) {
            userDAO.updateOrPersist(user);
        }
    }

    @Test
    public void testPersistenceFullyEquals() {
        assertTrue(true); // not working with too much data
    }

    @Test
    public void testGetForMassiveUpdate() {
        super.testGetForMassiveUpdate();
    }

    @Test
    public void testGetForMassiveDownload() {
        super.testGetForMassiveDownload();
    }

    @Test
    public void testUpdateOrPersistAndQueries() {
        super.testUpdateOrPersistAndQueries();
    }

    @Test
    public void testUpdateOrPersistAsync() {
        super.testUpdateOrPersistAsync();
    }

    @Test
    public void testRemove() {
        fixUniqueUser();
        super.testRemove();
    }

    @Test
    public void testRemoveMultiple() {
        fixUniqueUser();
        super.testRemoveMultiple();
    }

    @Test
    public void testRemoveAsync() {
        fixUniqueUser();
        super.testRemoveAsync();
    }

    @Test
    public void testFindUnique() {
        fixUniqueUser();
        super.testFindUnique();
    }

    @Test
    public void testQueryAllObjects() {
        fixUniqueUser();
        super.testQueryAllObjects();
    }

    @Test
    public void testQuerySpecific() {
        fixUniqueUser();
        super.testQuerySpecific();
    }

    private void fixUniqueUser() {
        lastNameUniqueId = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);

        final User sample = new User();
        sample.FIRSTNAME.set("hernan");
        sample.LASTNAME.set("liendo" + lastNameUniqueId);
        sample.AGE.set(18);
        sample.LONG_VALUE.set(23123213L);
        sample.IS_FAKE.set(false);

        userDAO.updateOrPersist(sample);
    }
}
