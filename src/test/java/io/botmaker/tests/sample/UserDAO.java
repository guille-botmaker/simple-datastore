package io.botmaker.tests.sample;

import io.botmaker.simpleredis.dao.DAO;

public final class UserDAO extends DAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public User findByLastName(final String lastName) {
        return findUniqueByIndexableProperty(sample.LASTNAME.getPropertyName(), lastName);
    }

    public User findByAge(final int age) {
        return findUniqueByIndexableProperty(sample.AGE.getPropertyName(), "" + age);
    }
}
