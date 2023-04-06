package pet.store.runtime;

import org.springframework.http.ResponseEntity;

public interface ErrorHandler {

    default ResponseEntity<?> errorResponse(Throwable e) {
        int statusCode = Util.statusCode(e);
        return ResponseEntity.status(statusCode).body(errorResponseBody(statusCode, e));
    }

    default Object errorResponseBody(int statusCode, Throwable e) {
        return new DefaultError(statusCode, e);
    }
}
