package pet.store;

import org.springframework.http.ResponseEntity;

import pet.store.model.Path_pets_Get_200_Response;

public interface Service {

    Path_pets_Get_200_Response pets(int limit) throws ServiceException;
    
    ResponseEntity<?> errorResponse(Throwable e);
    
}
