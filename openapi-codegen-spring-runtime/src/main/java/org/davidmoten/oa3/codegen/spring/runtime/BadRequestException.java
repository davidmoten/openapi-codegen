package org.davidmoten.oa3.codegen.spring.runtime;

public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 3643936430430630835L;

    public BadRequestException(String message) {
        super(message);
    }
    
}
