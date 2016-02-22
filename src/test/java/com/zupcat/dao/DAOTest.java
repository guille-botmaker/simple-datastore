package com.zupcat.dao;

import com.google.appengine.api.datastore.Query;
import com.zupcat.AbstractTest;
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

    protected String lastNameUniqueId = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
    protected UserDAO userDAO;

    @Parameterized.Parameters
    public static java.util.List<Object[]> data() {
        return Arrays.asList(new Object[2][0]);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        userDAO = service.getDAO(UserDAO.class);
//        service.configRemoteDatastore("m-infra", "437766657115-compute@developer.gserviceaccount.com", "C:/dev/m/mjava/websockets-server/Messengero-042bd9fe9f87.p12", false);

        for (final User user : buildUsers(lastNameUniqueId)) {
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

        final List<User> source = buildUsers(lastNameUniqueId);

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
        final List<User> prev = buildUsers(lastNameUniqueId);

        final int prevSize = prev.size();
        userDAO.massiveUpload(prev);

        RetryingHandler.sleep(5000);

        final List<User> all = userDAO.getAll();

        final int postSize = all.size();

        assertTrue(postSize >= prevSize + 5);

        assertTrue(all.iterator().next().ADDRESS.get().getStreet().startsWith("Sesamo"));
        assertTrue(all.iterator().next().ADDRESSES.iterator().next().getStreet().startsWith("Sesamo"));
        assertTrue(all.iterator().next().ADDRESSES_MAP.get(all.iterator().next().ADDRESSES.iterator().next().getStreet()).getStreet().startsWith("Sesamo"));
    }

//    @Test
//    public void testGetForMassiveDownload() {
//        int totalEntities = 0;
//        boolean specificFound = false;
//
//        for (int i = 0; i < DatastoreEntity.MAX_GROUPS; i++) {
//            final MassiveDownload massiveDownload = new MassiveDownload();
//            massiveDownload.setGroupId(i);
//            massiveDownload.setKind(userDAO.getEntityName());
//
//            userDAO.getForMassiveDownload(massiveDownload);
//
//            final Collection<PersistentObject> results = massiveDownload.getResults();
//
//            totalEntities += results.size();
//
//            for (final PersistentObject user : results) {
//                if (((User) user).LASTNAME.get().equals("liendo" + lastNameUniqueId)) {
//                    specificFound = true;
//                }
//            }
//        }
//
//        final int allSize = userDAO.getAll().size();
//        assertTrue(specificFound);
//        assertTrue(totalEntities >= 5 && totalEntities == allSize);
//    }

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
        final String id = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);

        assertTrue(userDAO.getByLastName("NewLastName" + id).size() == 0);

        final User user = new User();
        user.LASTNAME.set("NewLastName" + id);
        userDAO.updateOrPersistAsync(user);

        RetryingHandler.sleep(5000);

        assertTrue(userDAO.getByLastName("NewLastName" + id).size() == 1);
    }

    @Test
    public void testRemove() {
        final List<User> allUsers = userDAO.getByLastName("liendo" + lastNameUniqueId);
        assertTrue(allUsers.size() == 1);

        userDAO.remove(allUsers.iterator().next().getId());

        assertTrue(userDAO.getByLastName("liendo" + lastNameUniqueId).size() == 0);
    }

    @Test
    public void testRemoveMultiple() {
        final List<User> allUsers = userDAO.getByLastName("liendo" + lastNameUniqueId);
        assertTrue(allUsers.size() == 1);

        final List<String> ids = new ArrayList<>();
        ids.add(allUsers.iterator().next().getId());

        userDAO.remove(ids);

        assertTrue(userDAO.getByLastName("liendo" + lastNameUniqueId).size() == 0);
    }

    @Test
    public void testRemoveAsync() {
        final List<User> allUsers = userDAO.getByLastName("liendo" + lastNameUniqueId);
        assertTrue(allUsers.size() == 1);

        userDAO.removeAsync(allUsers.iterator().next().getId());

        RetryingHandler.sleep(5000);

        assertTrue(userDAO.getByLastName("liendo" + lastNameUniqueId).size() == 0);
    }

    @Test
    public void testFindUnique() {
        final Query query = new Query(userDAO.getEntityName());
        final User sample = new User();
        query.setFilter(new Query.FilterPredicate(sample.LASTNAME.getPropertyName(), Query.FilterOperator.EQUAL, "liendo" + lastNameUniqueId));

        final User result = userDAO.findUnique(query);
        assertEquals(result.LASTNAME.get(), "liendo" + lastNameUniqueId);
    }

    @Test
    public void testQueryAllObjects() {
        final List<User> allUsers = userDAO.getAll();

        assertFalse(allUsers.isEmpty());
    }

    @Test
    public void testQuerySpecific() {
        final List<User> allUsers = userDAO.getByLastName("liendo" + lastNameUniqueId);

        assertTrue(allUsers.size() == 1);
        checkSpecificUser(allUsers.get(0));

        checkSpecificUser(userDAO.findById(allUsers.get(0).getId()));
    }

    private void checkSpecificUser(final User user) {
        assertEquals(user.FIRSTNAME.get(), "hernan");
        assertEquals(user.LASTNAME.get(), "liendo" + lastNameUniqueId);
        assertEquals(user.AGE.get(), Integer.valueOf(18));
        assertEquals(user.IS_FAKE.get(), Boolean.FALSE);
    }
}
