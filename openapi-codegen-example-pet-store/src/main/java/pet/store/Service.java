package pet.store;

import org.springframework.http.ResponseEntity;

import pet.store.model.Path_pets_Get_200_Response;

// this would be generated code

public interface Service {

    Path_pets_Get_200_Response pets(int limit) throws ServiceException;

    default ResponseEntity<?> errorResponse(Throwable e) {
        final int statusCode;
        if (e instanceof ServiceException) {
            statusCode = ((ServiceException) e).statusCode();
        } else if (e instanceof IllegalArgumentException) {
            statusCode = 400;
        } else {
            statusCode = 500;
        }
        return ResponseEntity.status(statusCode).body(errorResponseBody(statusCode, e));
    }
    
    default Object errorResponseBody(int statusCode, Throwable e) {
        return new DefaultError(statusCode, e);
    }

}
