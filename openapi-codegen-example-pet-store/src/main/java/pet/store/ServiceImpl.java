package pet.store;

import java.util.Collections;

import org.springframework.http.ResponseEntity;

import pet.store.model.Error;
import pet.store.model.Path_pets_Get_200_Response;

public class ServiceImpl implements Service {

    @Override
    public Path_pets_Get_200_Response pets(int limit) {
        return new Path_pets_Get_200_Response(Collections.emptyList());
    }

    @Override
    public ResponseEntity<?> errorResponse(Throwable e) {
        if (e instanceof ServiceException) {
            ServiceException ex = (ServiceException) e;
            return ResponseEntity.status(ex.statusCode()).body(new Error(ex.getMessage()));
        } else if (e instanceof IllegalArgumentException) {
            IllegalArgumentException ex = (IllegalArgumentException) e;
            return ResponseEntity.status(400).body(new Error(ex.getMessage()));
        } else {
            return ResponseEntity.status(500).body(new Error(e.getMessage()));
        }
    }

}
