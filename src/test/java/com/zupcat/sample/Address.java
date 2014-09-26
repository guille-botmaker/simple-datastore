package com.zupcat.sample;

/**
 * Subobjects don't have own state. They should delegate its state to ObjectVar, just like this example
 */
public final class Address extends ObjectVar {

    public String getStreet() {
        return getString("s");
    }

    public void setStreet(final String street) {
        set("s", street);
    }

    public String getNumber() {
        return getString("n");
    }

    public void setNumber(final String number) {
        set("n", number);
    }

    public int getOrder() {
        return getInteger("o");
    }

    public void setOrder(final int order) {
        set("o", order);
    }
}
