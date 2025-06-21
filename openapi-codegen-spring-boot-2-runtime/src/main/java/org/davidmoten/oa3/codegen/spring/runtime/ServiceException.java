package org.davidmoten.oa3.codegen.spring.runtime;

import java.util.Optional;

import org.springframework.http.ResponseEntity;

public final class ServiceException extends Exception {

    private static final long serialVersionUID = -4115693210890378905L;

    private final int statusCode;
    private final Optional<? extends ResponseEntity<?>> response;

    public ServiceException(int statusCode, String message, Optional<? extends ResponseEntity<?>> response) {
        super(message);
        this.statusCode = statusCode;
        this.response = response;
    }

    public ServiceException(int statusCode, String message) {
        this(statusCode, message, Optional.empty());
    }

    public ServiceException(int statusCode, Throwable e) {
        super(e);
        this.statusCode = statusCode;
        this.response = Optional.empty();
    }

    public ServiceException(ResponseEntity<?> response) {
        this(response.getStatusCode().value(), response.getStatusCode().toString(), Optional.of(response));
    }

    public int statusCode() {
        return statusCode;
    }

    public Optional<? extends ResponseEntity<?>> response() {
        return response;
    }

}
