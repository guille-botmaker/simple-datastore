package io.botmaker.tests.dao;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.botmaker.simpleredis.dao.RetryingHandler;
import io.botmaker.simpleredis.model.DataObject;
import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.util.RandomUtils;
import io.botmaker.tests.AbstractTest;
import io.botmaker.tests.sample.ABean;
import io.botmaker.tests.sample.Address;
import io.botmaker.tests.sample.User;
import io.botmaker.tests.sample.UserDAO;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
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

    private static ObjectMapper getObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        userDAO = service.getDAO(UserDAO.class);

        userDAO.remove(userDAO.getAll().stream().map(e -> e.getId()).collect(Collectors.toList()));

        for (final User user : buildUsers(lastNameUniqueId)) {
            userDAO.save(user);
        }
    }

    @Test
    public void testListJSONObjectPropertyGetItems() {

        final User user = new User();

        final Address address1 = new Address();
        address1.setStreet("Sesamo Street");
        address1.setNumber("1st");
        address1.setOrder(222);

        final DataObject address2 = new DataObject();
        address2.mergeWith(address1);

        address1.setOrder(111);

        user.ADDRESSES.add(address1);

        final JSONArray jsonArray = user.getDataObject().getJSONArray(user.ADDRESSES.getPropertyName());
        jsonArray.put(address2);

        Assert.assertEquals(111, address1.getOrder());
        Assert.assertEquals(222, address2.get("o"));

        for (final Address item : user.ADDRESSES.getItemsReadonly()) {
            System.err.println(item.getStreet() + " " + item.getNumber());
            if (item.getOrder() == 222) {
                item.setOrder(333); // change converted object (dataobject to address)
            }
        }

        Assert.assertEquals(111, address1.getOrder());
        Assert.assertEquals(333, address2.get("o"));
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
    public void testJsonProperty() {
        final Map<String, Object> inner = new HashMap<>();
        inner.put("is1", "is1");
        inner.put("ii22", 22);

        final User user = new User();
        user.setId(RandomUtils.getInstance().getRandomSafeAlphaNumberString(10));
        user.LASTNAME.set("TestUser1");
        user.JSON_PROPERTY.put("s1", "1");
        user.JSON_PROPERTY.put("s2", "2");
        user.JSON_PROPERTY.put("i1", 1);
        user.JSON_PROPERTY.put("i2", 2);
        user.JSON_PROPERTY.put("inner", inner);

        userDAO.save(user);
        final User dbUser = userDAO.findById(user.getId());
        assertEquals(user, dbUser);
        assertEquals(user.getDataObjectForClient().toString(), dbUser.getDataObjectForClient().toString());
        assertEquals(user.JSON_PROPERTY.get("s2").toString(), "2");
        assertEquals(dbUser.JSON_PROPERTY.get("s2").toString(), "2");
    }

    @Test
    public void testUniqueAndNonUniqueIndexableProperties() {
        final String id1 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id1));

        final User user1 = new User();
        user1.setId(id1);
        user1.LASTNAME.set("TestUser1");
        user1.AGE.set(150);
        user1.STATE.set("new");
        userDAO.save(user1);
        assertEquals(user1, userDAO.findById(id1));

        final String id2 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id2));

        final User user2 = new User();
        user2.setId(id2);
        user2.LASTNAME.set("TestUser2");
        user2.AGE.set(150);
        user2.STATE.set("new");
        userDAO.save(user2);
        assertEquals(user1, userDAO.findById(id1));
        assertEquals(user2, userDAO.findById(id2));

        // find by indexable (unique!) property
        final User userByLastName1 = userDAO.findByLastName(user1.LASTNAME.get());
        final User userByLastName2 = userDAO.findByLastName(user2.LASTNAME.get());
        assertEquals(user1, userByLastName1);
        assertEquals(user2, userByLastName2);

        // find by indexable (not unique) property
        final List<User> usersbyAge = userDAO.findByAge(150);
        assertEquals(2, usersbyAge.size());
        assertTrue(usersbyAge.contains(user1));
        assertTrue(usersbyAge.contains(user2));

        //create a new entity with the same indexable unique property (SAME LAST NAME)
        final String id3 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id3));

        final User user3 = new User();
        user3.setId(id3);
        user3.LASTNAME.set("TestUser1");
        user3.AGE.set(150);
        user3.STATE.set("new");
        userDAO.save(user3);
        assertNull(userDAO.findById(id1));
        assertEquals(user2, userDAO.findById(id2));
        assertEquals(user3, userDAO.findById(id3));

        // change state
        user3.STATE.set("old");
        userDAO.save(user3);

        assertEquals(1, userDAO.findByState("new").size());
        assertEquals(1, userDAO.findByState("old").size());
    }

    @Test
    public void testUniqueAndNonUniqueIndexablePropertiesMultiple() {
        final String id1 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id1));
        final User user1 = new User();
        user1.setId(id1);
        user1.LASTNAME.set("TestUser1");
        user1.AGE.set(150);
        user1.STATE.set("new");

        final String id2 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id2));
        final User user2 = new User();
        user2.setId(id2);
        user2.LASTNAME.set("TestUser2");
        user2.AGE.set(150);
        user2.STATE.set("new");

        userDAO.massiveUpload(Arrays.asList(user1, user2));
        assertEquals(user1, userDAO.findById(id1));
        assertEquals(user2, userDAO.findById(id2));

        // find by indexable (unique!) property
        final User userByLastName1 = userDAO.findByLastName(user1.LASTNAME.get());
        final User userByLastName2 = userDAO.findByLastName(user2.LASTNAME.get());
        assertEquals(user1, userByLastName1);
        assertEquals(user2, userByLastName2);

        // find by indexable (not unique) property
        final List<User> usersbyAge = userDAO.findByAge(150);
        assertEquals(2, usersbyAge.size());
        assertTrue(usersbyAge.contains(user1));
        assertTrue(usersbyAge.contains(user2));

        //create a new entity with the same indexable unique property (SAME LAST NAME)
        final String id3 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id3));

        final User user3 = new User();
        user3.setId(id3);
        user3.LASTNAME.set("TestUser1");
        user3.AGE.set(150);
        user3.STATE.set("new");

        // change state
        user2.STATE.set("old");

        // save massively
        userDAO.massiveUpload(Arrays.asList(user3, user2));

        assertNull(userDAO.findById(id1));
        assertEquals(user2, userDAO.findById(id2));
        assertEquals(user3, userDAO.findById(id3));
        assertEquals(1, userDAO.findByState("new").size());
        assertEquals(1, userDAO.findByState("old").size());
    }

    @Test
    public void testNULLPlaceholderProperty() {

        final User u = new User();
        u.getDataObject().put(u.ADDRESS.getPropertyName(), JSONObject.NULL);

        u.ADDRESS.get(); // this used to fail, to fix it we have added an override of "has" method in DataObject

        u.getDataObject().getInternalMap().put(u.ADDRESS.getPropertyName(), JSONObject.NULL);

        u.ADDRESS.get(); // this used to fail, to fix it we have added an override of "has" method in DataObject
    }

    @Test
    public void testLastOccurrencesByIndexableProperty() {
        for (int i = 0; i < 10; i++) {
            final User u = new User();
            u.setNewId();
            u.LASTNAME.set("TestUserLastName" + i);
            u.AGE.set(150);
            u.STATE.set("old");
            userDAO.save(u);
        }

        final List<User> result = userDAO.findMultipleLastOccurrencesByIndexableProperty(null, userDAO.getSample().AGE.getPropertyName(), 2, "150");
        assertEquals(2, result.size());
        assertEquals("TestUserLastName9", result.get(1).LASTNAME.get());
    }

    @Test
    public void testObjectProperty() {
        final String id1 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id1));

        final User user1 = new User();
        user1.setId(id1);
        user1.LASTNAME.set("TestUser1");
        user1.AGE.set(150);
        user1.STATE.set("old");
        user1.SAMPLE_ARBITRARY_OBJECT.set(new ABean("1", 1));
        user1.SAMPLE_ARBITRARY_OBJECT_COMP.set(new ABean("1", 1));
        userDAO.save(user1);

        final User user2 = userDAO.findById(id1);
        assertEquals(user1, user2);
        assertTrue(user2.SAMPLE_ARBITRARY_OBJECT.get().s.equals("1"));
        assertTrue(user2.SAMPLE_ARBITRARY_OBJECT.get().i == 1);
        assertTrue(user2.SAMPLE_ARBITRARY_OBJECT_COMP.get().s.equals("1"));
        assertTrue(user2.SAMPLE_ARBITRARY_OBJECT_COMP.get().i == 1);
    }

    @Test
    public void testFindMultipleIntersectionOfIndexableProperty() {
        final String id1 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id1));

        final User user1 = new User();
        user1.setId(id1);
        user1.LASTNAME.set("TestUser1");
        user1.AGE.set(150);
        user1.STATE.set("old");
        user1.SAMPLE_ARBITRARY_OBJECT.set(new ABean("1", 1));
        userDAO.save(user1);
        assertEquals(user1, userDAO.findById(id1));

        final String id2 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id2));

        final User user2 = new User();
        user2.setId(id2);
        user2.LASTNAME.set("TestUser2");
        user2.AGE.set(150);
        user2.STATE.set("new");
        userDAO.save(user2);
        assertEquals(user2, userDAO.findById(id2));

        final String id3 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id3));

        final User user3 = new User();
        user3.setId(id3);
        user3.LASTNAME.set("TestUser3");
        user3.AGE.set(150);
        user3.STATE.set("old");
        userDAO.save(user3);
        assertEquals(user3, userDAO.findById(id3));

        HashMap<String, String> propertyNameAndValueMap = new HashMap<>(2);
        propertyNameAndValueMap.put(user1.STATE.getPropertyName(), "old");
        propertyNameAndValueMap.put(user1.AGE.getPropertyName(), "150");
        List<User> userList = userDAO.findMultipleIntersectionOfIndexableProperty(null, propertyNameAndValueMap);
        assertEquals(2, userList.size());
        assertTrue(userList.contains(user1));
        assertFalse(userList.contains(user2));
        assertTrue(userList.contains(user3));

        propertyNameAndValueMap = new HashMap<>(2);
        propertyNameAndValueMap.put(user1.STATE.getPropertyName(), "new");
        propertyNameAndValueMap.put(user1.AGE.getPropertyName(), "150");
        userList = userDAO.findMultipleIntersectionOfIndexableProperty(null, propertyNameAndValueMap);
        assertEquals(1, userList.size());
        assertFalse(userList.contains(user1));
        assertTrue(userList.contains(user2));
        assertFalse(userList.contains(user3));
    }

    @Test
    public void testFindMultipleUnionOfIndexableProperty() {
        final String id1 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id1));

        final User user1 = new User();
        user1.setId(id1);
        user1.LASTNAME.set("TestUser1");
        user1.AGE.set(10);
        user1.STATE.set("old");
        user1.SAMPLE_ARBITRARY_OBJECT.set(new ABean("1", 1));
        userDAO.save(user1);
        assertEquals(user1, userDAO.findById(id1));

        final String id2 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id2));

        final User user2 = new User();
        user2.setId(id2);
        user2.LASTNAME.set("TestUser2");
        user2.AGE.set(20);
        user2.STATE.set("new");
        userDAO.save(user2);
        assertEquals(user2, userDAO.findById(id2));

        final String id3 = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id3));

        final User user3 = new User();
        user3.setId(id3);
        user3.LASTNAME.set("TestUser3");
        user3.AGE.set(20);
        user3.STATE.set("old");
        userDAO.save(user3);
        assertEquals(user3, userDAO.findById(id3));

        ArrayList<Pair<String, String>> propertyNameAndValueMap = new ArrayList<>(3);
        propertyNameAndValueMap.add(Pair.of(user1.STATE.getPropertyName(), "old"));
        propertyNameAndValueMap.add(Pair.of(user1.AGE.getPropertyName(), "10"));
        propertyNameAndValueMap.add(Pair.of(user1.AGE.getPropertyName(), "20"));
        List<User> userList = userDAO.findMultipleUnionOfIndexableProperty(propertyNameAndValueMap);
        assertEquals(3, userList.size());
        assertTrue(userList.contains(user1));
        assertTrue(userList.contains(user2));
        assertTrue(userList.contains(user3));

        propertyNameAndValueMap = new ArrayList<>(2);
        propertyNameAndValueMap.add(Pair.of(user1.STATE.getPropertyName(), "new"));
        propertyNameAndValueMap.add(Pair.of(user1.AGE.getPropertyName(), "10"));
        userList = userDAO.findMultipleUnionOfIndexableProperty(propertyNameAndValueMap);
        assertEquals(2, userList.size());
        assertTrue(userList.contains(user1));
        assertTrue(userList.contains(user2));
        assertFalse(userList.contains(user3));
    }

    @Test
    public void testGetAll() {

        assertEquals(6, userDAO.getAll().size());

        final String id = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);
        assertNull(userDAO.findById(id));

        final User user = new User();
        user.setId(id);
        user.LASTNAME.set("Test123");
        user.AGE.set(150);
        userDAO.save(user);

        assertEquals(7, userDAO.getAll().size());
    }

    @Test
    public void testSaveAndQueries() {
        final String id = RandomUtils.getInstance().getRandomSafeAlphaNumberString(10);

        assertNull(userDAO.findById(id));

        final User user = new User();
        user.setId(id);
        user.LASTNAME.set("Test123");
        user.AGE.set(150);
        userDAO.save(user);

        assertEquals(user, userDAO.findById(id));

        // find by id
        User next = userDAO.findById(id);
        assertEquals(user, next);

        // find by id multiple
        next = userDAO.findUniqueIdMultiple(Arrays.asList(id)).values().iterator().next();
        assertEquals(user, next);

        // find by indexable (unique!) property
        next = userDAO.findByLastName(user.LASTNAME.get());
        assertEquals(user, next);

        // find by indexable (not unique) property
        final List<User> usersByAge = userDAO.findByAge(150);
        assertEquals(1, usersByAge.size());
        assertEquals(user, usersByAge.get(0));
    }

    @Test
    public void testRemove() {

        User theuser = userDAO.findByLastName("liendo" + lastNameUniqueId);
        assertNotNull(theuser);

        userDAO.remove(theuser.getId());

        theuser = userDAO.findByLastName("liendo" + lastNameUniqueId);
        assertNull(theuser);
    }

    @Test
    public void testRemoveMultiple() {
        User theuser = userDAO.findByLastName("liendo" + lastNameUniqueId);
        assertNotNull(theuser);

        final List<String> ids = new ArrayList<>();
        ids.add(theuser.getId());

        userDAO.remove(ids);

        theuser = userDAO.findByLastName("liendo" + lastNameUniqueId);
        assertNull(theuser);

        // test remove all
        assertEquals(5, userDAO.getAll().size());
        userDAO.remove(userDAO.getAll().stream().map(e -> e.getId()).collect(Collectors.toList()));
        assertEquals(0, userDAO.getAll().size());
    }

    @Test
    public void testFindUnique() {
        final User sample = new User();
        final User result = userDAO.findUniqueByIndexableProperty(null, sample.LASTNAME.getPropertyName(), "liendo" + lastNameUniqueId);

        assertEquals(result.LASTNAME.get(), "liendo" + lastNameUniqueId);
    }

    @Test
    public void testOrderedSet() {
        final RetryingHandler retryingHandler = userDAO.getRetryingHandler();
        retryingHandler.tryDSOrderedSetPut(1, "user1", "{ name: 'fer1'}", "TCustomer:B1", false);
        retryingHandler.tryDSOrderedSetPut(2, "user1", "{ name: 'fer1'}", "TCustomer:B1", false);
        retryingHandler.tryDSOrderedSetPut(1, "user2", "{ name: 'fer2'}", "TCustomer:B1", false);
        retryingHandler.tryDSOrderedSetPut(1, "user3", "{ name: 'fer3'}", "TCustomer:B1", false);
        retryingHandler.tryDSOrderedSetPut(3, "user4", "{ name: 'fer4'}", "TCustomer:B2", false);
        retryingHandler.tryDSOrderedSetPut(3, "user5", "{ name: 'fer5'}", "TCustomer:B2", false);
        retryingHandler.tryDSOrderedSetPut(3, "user6", "{ name: 'fer6'}", "TCustomer:B2", false);

        final List<String> strings = retryingHandler.tryDSOrderedSetGetUnion("TCustomer", Arrays.asList("B1", "B2"), 2, false);

        assertEquals(2, strings.size());
        assertTrue(strings.get(0).contains("fer6"));
        assertTrue(strings.get(1).contains("fer5"));
    }

    @Test
    public void testQuerySpecific() {
        final User theUser = userDAO.findByLastName("liendo" + lastNameUniqueId);

        assertNotNull(theUser);
        checkSpecificUser(theUser);
    }

    @Test
    public void testRedisEntityIsSerializableWithJackson() {
        final User theUser = userDAO.findByLastName("liendo" + lastNameUniqueId);

        final TypeReference<User> userTypeReference = new TypeReference<User>() {
        };

        try {
            final ObjectWriter objectWriter = getObjectMapper().writerFor(userTypeReference);
            final String userString = objectWriter.writeValueAsString(theUser);
            System.err.println("User: " + userString);
            final ObjectMapper objectMapper = getObjectMapper();
            final Object userCopy = objectMapper.readValue(userString, userTypeReference);

            Assert.assertEquals(userString, objectWriter.writeValueAsString(userCopy));
        } catch (final java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkSpecificUser(final User user) {
        assertEquals(user.FIRSTNAME.get(), "hernan");
        assertEquals(user.LASTNAME.get(), "liendo" + lastNameUniqueId);
        assertEquals(user.AGE.get(), Integer.valueOf(18));
        assertEquals(user.IS_FAKE.get(), Boolean.FALSE);
    }
}
