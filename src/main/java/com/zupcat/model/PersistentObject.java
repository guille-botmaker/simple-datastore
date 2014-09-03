package com.zupcat.model;

import com.zupcat.util.RandomUtils;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Base class for all persistent entities. Handles data and metadata of business objects
 */
public abstract class PersistentObject implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    protected static final Logger LOGGER = Logger.getLogger(PersistentObject.class.getName());

    public static final String DATE_FORMAT = "yyMMddHHmmssSSS";

    protected String id;


    protected PersistentObject() {
        setId(RandomUtils.getInstance().getRandomSafeAlphaNumberString(20));
    }


    public abstract void setId(final String id);

    public abstract void setModified();


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PersistentObject that = (PersistentObject) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
