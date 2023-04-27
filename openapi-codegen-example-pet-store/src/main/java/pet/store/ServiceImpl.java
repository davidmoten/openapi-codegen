package pet.store;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import pet.store.path.PetsGet200Response;
import pet.store.schema.NewPet;
import pet.store.schema.Pet;
import pet.store.schema.PetId;
import pet.store.service.Service;

@Component
public class ServiceImpl implements Service {

    @Override
    public PetsGet200Response petsGet(List<String> tags, int limit) throws ServiceException {
        System.out.println(tags);
        long t = System.currentTimeMillis();
        if (t % 3 == 0) {
            throw new ServiceException(405, "something went wrong");
        } else if (t % 3 == 1) {
            return response(ResponseEntity.status(409).body("problem"));
        }
        if (limit != 3) {
            Pet pet = new Pet(new NewPet("fido", Optional.empty()), new PetId(123));
            return new PetsGet200Response(Collections.singletonList(pet));
        } else {
            return new PetsGet200Response(Collections.emptyList());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // override errorResponseBody method to use your own error object from openapi
    // definition
    ////////////////////////////////////////////////////////////////////////////////////

}
