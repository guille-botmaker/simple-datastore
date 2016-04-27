package io.botmaker.tests.sample;

import io.botmaker.simpleredis.dao.DAO;

public final class UserDAO extends DAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public User getByLastName(final String lastName) {
        return findUniqueByIndexableProperty(sample.LASTNAME.getPropertyName(), lastName);
    }
}
