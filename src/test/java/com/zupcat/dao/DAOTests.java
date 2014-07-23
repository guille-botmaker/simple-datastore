package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;

import java.util.List;

public class DAOTests extends AbstractTest {


    public void testQueryAllObjects() {
        final List<SampleUser> allUsers = service.getDAO(SampleUserDAO.class).getAll();

        assertFalse(allUsers.isEmpty());
    }
}
