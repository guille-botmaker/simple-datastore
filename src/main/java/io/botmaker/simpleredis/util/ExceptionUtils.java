package io.botmaker.simpleredis.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {

    public static String exceptionToString(final Throwable ex) {
        final StringWriter exceptionWriter = new StringWriter(3000);
        ex.printStackTrace(new PrintWriter(exceptionWriter));

        Throwable inner = ex.getCause();
        while (inner != null) {
            exceptionWriter.append("\nCaused by: \n");
            inner.printStackTrace(new PrintWriter(exceptionWriter));

            inner = inner.getCause();
        }
        return exceptionWriter.toString();
    }
}
