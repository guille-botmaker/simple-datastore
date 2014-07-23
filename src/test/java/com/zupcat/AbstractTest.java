package com.zupcat;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;
import com.zupcat.service.SimpleDatastoreService;
import com.zupcat.service.SimpleDatastoreServiceFactory;
import junit.framework.TestCase;

import java.util.List;

public abstract class AbstractTest extends TestCase {

    protected SimpleDatastoreService service;
    protected final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        service = SimpleDatastoreServiceFactory.getSimpleDatastoreService();
        service.registerDAO(new SampleUserDAO());

        helper.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        helper.tearDown();
    }


    public void testQueryAllObjects() {
        final List<SampleUser> allUsers = service.getDAO(SampleUserDAO.class).getAll();

        assertFalse(allUsers.isEmpty());
    }
}
