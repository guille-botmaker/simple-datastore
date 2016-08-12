package io.botmaker.simpleredis.dao;

import io.botmaker.simpleredis.model.RedisEntity;

import java.util.List;

public interface DAOCustomByIdCache<P extends RedisEntity> {

    P get(final String id);

    void put(final String id, final P result);

    void setDAO(final DAO<P> dao);

    /**
     * @return true if wanting to abort normal saving
     */
    boolean alternativeSave(final P persistentObject);

    List<P> getByParams(final Object... params);

    void putByParams(final List<P> result, final Object... params);
}
