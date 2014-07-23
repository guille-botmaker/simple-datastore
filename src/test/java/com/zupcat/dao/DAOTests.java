package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;
import com.zupcat.util.RandomUtils;

import java.util.ArrayList;
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

    private static final List<SampleUser> buildUsers() {
        final int samples = 100;
        final List<SampleUser> result = new ArrayList<>(samples);
        final RandomUtils randomUtils = RandomUtils.getInstance();

        for (int i = 0; i < samples; i++) {
            final SampleUser sample = new SampleUser();
            sample.NAME.set("User Name " + randomUtils.getRandomSafeString(10));
            sample.AGE.set(randomUtils.getIntBetweenInclusive(1, 100));

            result.add(sample);
        }
        return result;
    }
}
