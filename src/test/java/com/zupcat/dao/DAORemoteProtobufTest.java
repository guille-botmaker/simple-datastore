package com.zupcat.dao;

import com.zupcat.sample.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

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

        for (final User user : buildUsers()) {
            userDAO.updateOrPersist(user);
        }
    }

    @Test
    public void testPersistenceFullyEquals() {
        super.testPersistenceFullyEquals();
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
        super.testRemove();
    }

    @Test
    public void testRemoveMultiple() {
        super.testRemoveMultiple();
    }

    @Test
    public void testRemoveAsync() {
        super.testRemoveAsync();
    }

    @Test
    public void testFindUnique() {
        super.testFindUnique();
    }

    @Test
    public void testQueryAllObjects() {
        super.testQueryAllObjects();
    }

    @Test
    public void testQuerySpecific() {
        super.testQuerySpecific();
    }
}
