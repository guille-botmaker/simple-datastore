package com.zupcat.dao;

import com.zupcat.sample.User;
import org.junit.Before;
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

//    @Test
//    public void testPersistenceFullyEquals() {
//    }
//
//    @Test
//    public void testGetForMassiveUpdate() {
//    }
//
//    @Test
//    public void testGetForMassiveDownload() {
//    }
//
//    @Test
//    public void testUpdateOrPersistAndQueries() {
//    }
//
//    @Test
//    public void testUpdateOrPersistAsync() {
//    }
//
//    @Test
//    public void testRemove() {
//    }
//
//    @Test
//    public void testRemoveMultiple() {
//    }
//
//    @Test
//    public void testRemoveAsync() {
//    }
//
//    @Test
//    public void testFindUnique() {
//    }
//
//    @Test
//    public void testQueryAllObjects() {
//    }
//
//    @Test
//    public void testQuerySpecific() {
//    }
}
