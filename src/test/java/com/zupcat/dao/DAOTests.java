package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;

import java.util.List;

public class DAOTests extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final SampleUserDAO sampleUserDAO = service.getDAO(SampleUserDAO.class);

        for (final SampleUser sampleUser : buildUsers()) {
            sampleUserDAO.updateOrPersist(sampleUser);
        }
    }

    public void testQueryAllObjects() {
        final List<SampleUser> allUsers = service.getDAO(SampleUserDAO.class).getAll();

        assertFalse(allUsers.isEmpty());
    }
}
