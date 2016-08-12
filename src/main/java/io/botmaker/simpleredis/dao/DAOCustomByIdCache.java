package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.model.RedisEntity;

public interface DAOCustomByIdCache<P extends RedisEntity> {

    P get(final String id);

    void put(final String id, final P result);

    void setDAO(final DAO<P> dao);
}
