package io.botmaker.tests.dao;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.util.RandomUtils;
import io.botmaker.tests.AbstractTest;
import io.botmaker.tests.sample.User;
import io.botmaker.tests.sample.UserDAO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        for (final User user : buildUsers(lastNameUniqueId)) {
            userDAO.updateOrPersist(user);
        }
    }

    @Test
    public void testPersistenceFullyEquals() {
        final Comparator<User> comparator = (o1, o2) -> o1.getId().compareTo(o2.getId());

        final List<User> source = buildUsers(lastNameUniqueId);
        source.sort(comparator);
        userDAO.massiveUpload(source);

        final List<User> target = new ArrayList<>(userDAO.findUniqueIdMultiple(source.stream().map(RedisEntity::getId).collect(Collectors.toList())).values());
        target.sort(comparator);

        // checking both are completely equals
        assertEquals(target.size(), source.size());

        for (int i = 0; i < source.size(); i++) {
            assertTrue(source.get(i).isFullyEquals(target.get(i)));
        }
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

        final List<String> ids = new ArrayList<>();
        ids.add(id);

        final User next = userDAO.findUniqueIdMultiple(ids).values().iterator().next();
        assertEquals(user, next);
    }

    @Test
    public void testRemove() {
        User theuser = userDAO.getByLastName("liendo" + lastNameUniqueId);
        assertNotNull(theuser);

        userDAO.remove(theuser.getId());

        theuser = userDAO.getByLastName("liendo" + lastNameUniqueId);
        assertNull(theuser);
    }

    @Test
    public void testRemoveMultiple() {
        User theuser = userDAO.getByLastName("liendo" + lastNameUniqueId);
        assertNotNull(theuser);

        final List<String> ids = new ArrayList<>();
        ids.add(theuser.getId());

        userDAO.remove(ids);

        theuser = userDAO.getByLastName("liendo" + lastNameUniqueId);
        assertNull(theuser);
    }

    @Test
    public void testFindUnique() {
        final User sample = new User();
        final User result = userDAO.findUniqueByIndexableProperty(sample.LASTNAME.getPropertyName(), "liendo" + lastNameUniqueId);

        assertEquals(result.LASTNAME.get(), "liendo" + lastNameUniqueId);
    }

    @Test
    public void testQuerySpecific() {
        final User theUser = userDAO.getByLastName("liendo" + lastNameUniqueId);

        assertNotNull(theUser);
        checkSpecificUser(theUser);
    }

    private void checkSpecificUser(final User user) {
        assertEquals(user.FIRSTNAME.get(), "hernan");
        assertEquals(user.LASTNAME.get(), "liendo" + lastNameUniqueId);
        assertEquals(user.AGE.get(), Integer.valueOf(18));
        assertEquals(user.IS_FAKE.get(), Boolean.FALSE);
    }
}
