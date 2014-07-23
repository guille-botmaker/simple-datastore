package com.zupcat.dao;

import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;
import com.zupcat.service.SimpleDatastoreService;
import com.zupcat.service.SimpleDatastoreServiceFactory;
import junit.framework.TestCase;

import java.util.List;

public class DAOTests extends TestCase {

    private SimpleDatastoreService service;

    @Override
    protected void setUp() throws Exception {
        service = SimpleDatastoreServiceFactory.getSimpleDatastoreService();
        service.registerDAO(new SampleUserDAO());
    }

    public void testQueryAllObjects() {
        final List<SampleUser> allUsers = service.getDAO(SampleUserDAO.class).getAll();

        assertFalse(allUsers.isEmpty());
    }
}
