package com.zupcat.sample;

import com.zupcat.model.DataObject;

/**
 * Subobjects don't have own state. They should delegate its state to DataObject, just like this example
 */
public final class Address extends DataObject {

    public String getStreet() {
        return getString("s");
    }

    public void setStreet(final String street) {
        put("s", street);
    }

    public String getNumber() {
        return getString("n");
    }

    public void setNumber(final String number) {
        put("n", number);
    }

    public int getOrder() {
        return getInt("o");
    }

    public void setOrder(final int order) {
        put("o", order);
    }
}
