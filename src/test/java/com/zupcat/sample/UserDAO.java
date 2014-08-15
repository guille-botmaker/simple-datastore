package com.zupcat.sample;

import com.google.appengine.api.datastore.Query;
import com.zupcat.dao.DAO;

import java.util.List;

public final class UserDAO extends DAO<User> {

    public UserDAO() {
        super(new User());
    }

    public List<User> getByLastName(final String lastName) {
        final Query query = new Query(getEntityName());
        query.setFilter(new Query.FilterPredicate(sample.LASTNAME.getPropertyName(), Query.FilterOperator.EQUAL, lastName));

        return findByQuery(query);
    }
}
