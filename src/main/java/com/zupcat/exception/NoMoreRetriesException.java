package com.zupcat.exception;

import java.io.Serializable;

public final class NoMoreRetriesException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -7034897190745766939L;

    public NoMoreRetriesException(final Throwable cause) {
        super(cause);
    }
}
