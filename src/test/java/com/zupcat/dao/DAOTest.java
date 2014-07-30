package com.zupcat.dao;

import com.google.appengine.api.datastore.Query;
import com.zupcat.AbstractTest;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.PersistentObject;
import com.zupcat.sample.SampleUser;
import com.zupcat.sample.SampleUserDAO;
import com.zupcat.util.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class DAOTest extends AbstractTest {

    private SampleUserDAO sampleUserDAO;

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[5][0]);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        sampleUserDAO = service.getDAO(SampleUserDAO.class);

        for (final SampleUser sampleUser : buildUsers()) {
            sampleUserDAO.updateOrPersist(sampleUser);
        }
    }

    @Test
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

    @Test
    public void testGetForMassiveUpdate() {
        final int prevSize = sampleUserDAO.getAll().size();
        sampleUserDAO.massiveUpload(buildUsers());

        RetryingHandler.sleep(5000);

        final int postSize = sampleUserDAO.getAll().size();

        assertTrue(postSize >= prevSize + 100);
    }

    @Test
    public void testGetForMassiveDownload() {
        int totalEntities = 0;
        boolean specificFound = false;

        for (int i = 0; i < DatastoreEntity.MAX_GROUPS; i++) {
            final MassiveDownload massiveDownload = new MassiveDownload();
            massiveDownload.setGroupId(i);
            massiveDownload.setKind(sampleUserDAO.getEntityName());

            sampleUserDAO.getForMassiveDownload(massiveDownload);

            final Collection<PersistentObject> results = massiveDownload.getResults();

            totalEntities += results.size();

            for (final PersistentObject user : results) {
                if (((SampleUser) user).LASTNAME.get().equals("liendo")) {
                    specificFound = true;
                }
            }
        }

        final int allSize = sampleUserDAO.getAll().size();
        assertTrue(specificFound);
        assertTrue(totalEntities >= 100 && totalEntities == allSize);
    }

    @Test
    public void testUpdateOrPersistAndQueries() {
        final String id = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);

        assertNull(sampleUserDAO.findById(id));

        final SampleUser sampleUser = new SampleUser();
        sampleUser.setId(id);
        sampleUser.LASTNAME.set("Test123");

        sampleUserDAO.updateOrPersist(sampleUser);

        assertEquals(sampleUser, sampleUserDAO.findById(id));
        assertEquals(sampleUser, sampleUserDAO.findByIdAsync(id).get());

        final List<String> ids = new ArrayList<>();
        ids.add(id);

        assertEquals(sampleUser, sampleUserDAO.findUniqueIdMultiple(ids).values().iterator().next());
    }

    @Test
    public void testUpdateOrPersistAsync() {
        assertTrue(sampleUserDAO.getByLastName("NewLastName").size() == 0);

        final SampleUser sampleUser = new SampleUser();
        sampleUser.LASTNAME.set("NewLastName");
        sampleUserDAO.updateOrPersistAsync(sampleUser);

        RetryingHandler.sleep(5000);

        assertTrue(sampleUserDAO.getByLastName("NewLastName").size() == 1);
    }

    @Test
    public void testRemove() {
        final List<SampleUser> allUsers = sampleUserDAO.getByLastName("liendo");
        assertTrue(allUsers.size() == 1);

        sampleUserDAO.remove(allUsers.iterator().next().getId());

        assertTrue(sampleUserDAO.getByLastName("liendo").size() == 0);
    }

    @Test
    public void testRemoveMultiple() {
        final List<SampleUser> allUsers = sampleUserDAO.getByLastName("liendo");
        assertTrue(allUsers.size() == 1);

        final List<String> ids = new ArrayList<>();
        ids.add(allUsers.iterator().next().getId());

        sampleUserDAO.remove(ids);

        assertTrue(sampleUserDAO.getByLastName("liendo").size() == 0);
    }

    @Test
    public void testRemoveAsync() {
        final List<SampleUser> allUsers = sampleUserDAO.getByLastName("liendo");
        assertTrue(allUsers.size() == 1);

        sampleUserDAO.removeAsync(allUsers.iterator().next().getId());

        RetryingHandler.sleep(5000);

        assertTrue(sampleUserDAO.getByLastName("liendo").size() == 0);
    }

    @Test
    public void testFindUnique() {
        final Query query = new Query(sampleUserDAO.getEntityName());
        final SampleUser sample = new SampleUser();
        query.setFilter(new Query.FilterPredicate(sample.LASTNAME.getName(), Query.FilterOperator.EQUAL, "liendo"));

        final SampleUser result = sampleUserDAO.findUnique(query);
        assertEquals(result.LASTNAME.get(), "liendo");
    }

    @Test
    public void testQueryAllObjects() {
        final List<SampleUser> allUsers = sampleUserDAO.getAll();

        assertFalse(allUsers.isEmpty());
    }

    @Test
    public void testQuerySpecific() {
        final List<SampleUser> allUsers = sampleUserDAO.getByLastName("liendo");

        assertTrue(allUsers.size() == 1);
        checkSpecificUser(allUsers.get(0));

        checkSpecificUser(sampleUserDAO.findById(allUsers.get(0).getId()));
    }

    private void checkSpecificUser(final SampleUser sampleUser) {
        assertEquals(sampleUser.FIRSTNAME.get(), "hernan");
        assertEquals(sampleUser.LASTNAME.get(), "liendo");
        assertEquals(sampleUser.AGE.get(), Integer.valueOf(18));
        assertEquals(sampleUser.IS_FAKE.get(), Boolean.FALSE);
    }
}
