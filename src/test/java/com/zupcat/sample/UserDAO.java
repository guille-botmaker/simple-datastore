package com.zupcat.sample;

import com.google.appengine.api.datastore.Query;
import com.zupcat.dao.DAO;

import java.util.List;

public final class UserDAO extends DAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public List<User> getByLastName(final String lastName) {
        return findByQuery(new Query.FilterPredicate(sample.LASTNAME.getPropertyName(), Query.FilterOperator.EQUAL, lastName));
    }
}
