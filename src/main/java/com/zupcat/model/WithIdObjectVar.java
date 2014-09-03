package com.zupcat.model;

public abstract class WithIdObjectVar extends ObjectVar {

    private static final long serialVersionUID = 15286714167044223L;

    public WithIdObjectVar() {
    }

    public WithIdObjectVar(final ObjectVar anotherObjectVar) {
        super(anotherObjectVar);
    }

    public String getId() {
        return getString(AvroSerializer.ID_KEY);
    }

    public void setId(final String id) {
        set(AvroSerializer.ID_KEY, id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof WithIdObjectVar)) return false;

        final WithIdObjectVar that = (WithIdObjectVar) o;

        final String myId = getId();
        final String thatId = that.getId();

        return !(myId != null ? !myId.equals(thatId) : thatId != null);
    }

    @Override
    public int hashCode() {
        final String myId = getId();

        return myId != null ? myId.hashCode() : 0;
    }
}
