package com.zupcat.dao;

import com.google.appengine.api.datastore.Query;
import com.zupcat.AbstractTest;
import com.zupcat.model.DatastoreEntity;
import com.zupcat.model.PersistentObject;
import com.zupcat.sample.User;
import com.zupcat.sample.UserDAO;
import com.zupcat.util.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class DAOTest extends AbstractTest {

    private UserDAO userDAO;

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[2][0]);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        userDAO = service.getDAO(UserDAO.class);

        for (final User user : buildUsers()) {
            userDAO.updateOrPersist(user);
        }
    }

    @Test
    public void testPersistenceFullyEquals() {
        //cleaning db
        final List<String> ids = new ArrayList<>(100);
        for (final User user : userDAO.getAll()) {
            ids.add(user.getId());
        }

        userDAO.remove(ids);

        RetryingHandler.sleep(2000);

        assertTrue(userDAO.getAll().isEmpty());

        final List<User> source = buildUsers();

        userDAO.massiveUpload(source);

        RetryingHandler.sleep(2000);

        final List<User> target = userDAO.getAll();

        // checking both are completely equals
        assertEquals(target.size(), source.size());

        final Map<String, User> targetMap = new HashMap<>();
        for (final User user : target) {
            targetMap.put(user.getId(), user);
        }

        for (final User sourceUser : source) {
            final User targetUser = targetMap.get(sourceUser.getId());

            assertTrue(sourceUser.isFullyEquals(targetUser));
        }
    }

    @Test
    public void testGetForMassiveUpdate() {
        final List<User> prev = buildUsers();

        final int prevSize = prev.size();
        userDAO.massiveUpload(prev);

        RetryingHandler.sleep(5000);

        final List<User> all = userDAO.getAll();

        final int postSize = all.size();

        assertTrue(postSize >= prevSize + 100);

        assertTrue(all.iterator().next().ADDRESS.get().getStreet().startsWith("Sesamo"));
    }

    @Test
    public void testGetForMassiveDownload() {
        int totalEntities = 0;
        boolean specificFound = false;

        for (int i = 0; i < DatastoreEntity.MAX_GROUPS; i++) {
            final MassiveDownload massiveDownload = new MassiveDownload();
            massiveDownload.setGroupId(i);
            massiveDownload.setKind(userDAO.getEntityName());

            userDAO.getForMassiveDownload(massiveDownload);

            final Collection<PersistentObject> results = massiveDownload.getResults();

            totalEntities += results.size();

            for (final PersistentObject user : results) {
                if (((User) user).LASTNAME.get().equals("liendo")) {
                    specificFound = true;
                }
            }
        }

        final int allSize = userDAO.getAll().size();
        assertTrue(specificFound);
        assertTrue(totalEntities >= 100 && totalEntities == allSize);
    }

    @Test
    public void testUpdateOrPersistAndQueries() {
        final String id = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);

        assertNull(userDAO.findById(id));

        final User user = new User();
        user.setId(id);
        user.LASTNAME.set("Test123");

        userDAO.updateOrPersist(user);

        assertEquals(user, userDAO.findById(id));
        assertEquals(user, userDAO.findByIdAsync(id).get());

        final List<String> ids = new ArrayList<>();
        ids.add(id);

        final User next = userDAO.findUniqueIdMultiple(ids).values().iterator().next();
        assertEquals(user, next);
    }

    @Test
    public void testUpdateOrPersistAsync() {
        assertTrue(userDAO.getByLastName("NewLastName").size() == 0);

        final User user = new User();
        user.LASTNAME.set("NewLastName");
        userDAO.updateOrPersistAsync(user);

        RetryingHandler.sleep(5000);

        assertTrue(userDAO.getByLastName("NewLastName").size() == 1);
    }

    @Test
    public void testRemove() {
        final List<User> allUsers = userDAO.getByLastName("liendo");
        assertTrue(allUsers.size() == 1);

        userDAO.remove(allUsers.iterator().next().getId());

        assertTrue(userDAO.getByLastName("liendo").size() == 0);
    }

    @Test
    public void testRemoveMultiple() {
        final List<User> allUsers = userDAO.getByLastName("liendo");
        assertTrue(allUsers.size() == 1);

        final List<String> ids = new ArrayList<>();
        ids.add(allUsers.iterator().next().getId());

        userDAO.remove(ids);

        assertTrue(userDAO.getByLastName("liendo").size() == 0);
    }

    @Test
    public void testRemoveAsync() {
        final List<User> allUsers = userDAO.getByLastName("liendo");
        assertTrue(allUsers.size() == 1);

        userDAO.removeAsync(allUsers.iterator().next().getId());

        RetryingHandler.sleep(5000);

        assertTrue(userDAO.getByLastName("liendo").size() == 0);
    }

    @Test
    public void testFindUnique() {
        final Query query = new Query(userDAO.getEntityName());
        final User sample = new User();
        query.setFilter(new Query.FilterPredicate(sample.LASTNAME.getPropertyName(), Query.FilterOperator.EQUAL, "liendo"));

        final User result = userDAO.findUnique(query);
        assertEquals(result.LASTNAME.get(), "liendo");
    }

    @Test
    public void testQueryAllObjects() {
        final List<User> allUsers = userDAO.getAll();

        assertFalse(allUsers.isEmpty());
    }

    @Test
    public void testQuerySpecific() {
        final List<User> allUsers = userDAO.getByLastName("liendo");

        assertTrue(allUsers.size() == 1);
        checkSpecificUser(allUsers.get(0));

        checkSpecificUser(userDAO.findById(allUsers.get(0).getId()));
    }

    private void checkSpecificUser(final User user) {
        assertEquals(user.FIRSTNAME.get(), "hernan");
        assertEquals(user.LASTNAME.get(), "liendo");
        assertEquals(user.AGE.get(), Integer.valueOf(18));
        assertEquals(user.IS_FAKE.get(), Boolean.FALSE);
    }
}
