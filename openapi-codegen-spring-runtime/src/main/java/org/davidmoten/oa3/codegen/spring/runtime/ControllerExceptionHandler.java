package org.davidmoten.oa3.codegen.spring.runtime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface ControllerExceptionHandler {
    
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @ExceptionHandler(IllegalArgumentException.class)
    default void handleIllegalArgumentException() {
        // Nothing to do
    }

}
