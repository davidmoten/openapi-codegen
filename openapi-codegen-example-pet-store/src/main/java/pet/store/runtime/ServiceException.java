package pet.store.runtime;

import java.util.Optional;

import org.springframework.http.ResponseEntity;

public final class ServiceException extends Exception {

    private static final long serialVersionUID = -4115693210890378905L;

    private final int statusCode;
    private final Optional<ResponseEntity<?>> response;

    private ServiceException(int statusCode, String message, Optional<ResponseEntity<?>> response) {
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
        this(response.getStatusCodeValue(), response.getStatusCode().toString(), Optional.of(response));
    }

    public int statusCode() {
        return statusCode;
    }

    public Optional<ResponseEntity<?>> response() {
        return response;
    }

}
