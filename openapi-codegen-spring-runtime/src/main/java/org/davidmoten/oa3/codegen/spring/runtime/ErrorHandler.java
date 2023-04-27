package org.davidmoten.oa3.codegen.spring.runtime;

import org.davidmoten.oa3.codegen.spring.runtime.internal.Util;
import org.springframework.http.ResponseEntity;

public interface ErrorHandler {

    default ResponseEntity<?> errorResponse(Throwable e) {
        if (e instanceof ServiceException) {
            ServiceException ex = (ServiceException) e;
            if (ex.response().isPresent()) {
                return ex.response().get();
            }
        }
        int statusCode = Util.statusCode(e);
        return ResponseEntity.status(statusCode).body(errorResponseBody(statusCode, e));
    }

    default <T> T response(ResponseEntity<?> response) throws ServiceException {
        throw new ServiceException(response);
    }
    
    default Object errorResponseBody(int statusCode, Throwable e) {
        return new DefaultError(statusCode, e);
    }
    
    default ServiceException notImplemented() {
        return new ServiceException(501, "Not implemented");
    }
    
}
