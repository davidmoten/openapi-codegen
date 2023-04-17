package pet.store;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import pet.store.generated.Service;
import pet.store.path.PetsGet200Response;
import pet.store.runtime.ServiceException;
import pet.store.schema.NewPet;
import pet.store.schema.Pet;
import pet.store.schema.PetId;

@Component
public class ServiceImpl implements Service {

    @Override
    public PetsGet200Response pets(int limit) throws ServiceException {
        if (System.currentTimeMillis() % 2 == 0) {
            throw new ServiceException(405, "something went wrong");
        }
        Pet pet = new Pet(new NewPet("fido", Optional.empty()), new PetId(123));
        return new PetsGet200Response(Collections.singletonList(pet));
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // override errorResponseBody method to use your own error object from openapi
    // definition
    ////////////////////////////////////////////////////////////////////////////////////

}
