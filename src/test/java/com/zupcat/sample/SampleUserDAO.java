package com.zupcat.sample;

import com.google.appengine.api.datastore.Query;
import com.zupcat.dao.DAO;

import java.util.List;

public final class SampleUserDAO extends DAO<SampleUser> {

    public SampleUserDAO() {
        super(new SampleUser());
    }

    public List<SampleUser> getByLastName(final String lastName) {
        final Query query = new Query(getEntityName());
        query.setFilter(new Query.FilterPredicate(sample.LASTNAME.getPropertyName(), Query.FilterOperator.EQUAL, lastName));

        return findByQuery(query);
    }
}
