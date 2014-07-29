package com.zupcat.dao;

import com.zupcat.AbstractTest;
import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DAONoConcurrencyTest extends AbstractTest {

    private SampleUserDAO sampleUserDAO;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sampleUserDAO = service.getDAO(SampleUserDAO.class);

        for (final SampleUser sampleUser : buildUsers()) {
            sampleUserDAO.updateOrPersist(sampleUser);
        }
    }

    public void testPersistenceFullyEquals() {
        //cleaning db
        final List<String> ids = new ArrayList<>(100);
        for (final SampleUser sampleUser : sampleUserDAO.getAll()) {
            ids.add(sampleUser.getId());
        }

        sampleUserDAO.remove(ids);

        RetryingHandler.sleep(2000);

        assertTrue(sampleUserDAO.getAll().isEmpty());

        final List<SampleUser> source = buildUsers();

        sampleUserDAO.massiveUpload(source);

        RetryingHandler.sleep(2000);

        final List<SampleUser> target = sampleUserDAO.getAll();

        // checking both are completely equals
        assertEquals(target.size(), source.size());

        final Map<String, SampleUser> targetMap = new HashMap<>();
        for (final SampleUser sampleUser : target) {
            targetMap.put(sampleUser.getId(), sampleUser);
        }

        for (final SampleUser sourceUser : source) {
            final SampleUser targetUser = targetMap.get(sourceUser.getId());

            assertTrue(sourceUser.isFullyEquals(targetUser));
        }
    }
}
