package org.davidmoten.oa3.codegen.spring.runtime.internal;

import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;

public final class Util {

    public static int statusCode(Throwable e) {
        final int statusCode;
        if (e instanceof ServiceException) {
            statusCode = ((ServiceException) e).statusCode();
        } else if (e instanceof IllegalArgumentException) {
            statusCode = 400;
        } else {
            statusCode = 500;
        }
        return statusCode;
    }

}
