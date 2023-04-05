package pet.store;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pet.store.model.Error;
import pet.store.model.Path_pets_Get_200_Response;

@RestController
public class Service {

    @RequestMapping(method = RequestMethod.GET, value = "/pets", produces = { "application/json" })
    public ResponseEntity<?> pets() {
        try {
            return ResponseEntity.ofNullable(new Path_pets_Get_200_Response(Collections.emptyList()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(501).body(new Error(501, e.getMessage()));
        }
    }

}
