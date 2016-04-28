package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.model.Resource;

public final class ResourceDAO extends DAO<Resource> {

    public ResourceDAO() {
        super(Resource.class);
    }
}
