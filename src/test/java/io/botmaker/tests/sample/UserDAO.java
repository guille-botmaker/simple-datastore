package io.botmaker.tests.sample;

import io.botmaker.simpleredis.dao.DAO;

import java.util.List;

public final class UserDAO extends DAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public User findByLastName(final String lastName) {
        return findUniqueByIndexableProperty(sample.LASTNAME.getPropertyName(), lastName);
    }

    public List<User> findByAge(final int age) {
        return findMultipleByIndexableProperty(sample.AGE.getPropertyName(), "" + age);
    }

    public List<User> findByState(final String state) {
        return findMultipleByIndexableProperty(sample.STATE.getPropertyName(), state);
    }
}
