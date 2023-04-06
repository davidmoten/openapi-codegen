package pet.store;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import pet.store.generated.Service;
import pet.store.model.NewPet;
import pet.store.model.Path_pets_Get_200_Response;
import pet.store.model.Pet;
import pet.store.model.PetId;
import pet.store.runtime.ServiceException;

@Component
public class ServiceImpl implements Service {

    @Override
    public Path_pets_Get_200_Response pets(int limit) throws ServiceException {
        if (System.currentTimeMillis() % 2 == 0) {
            throw new ServiceException(405, "something went wrong");
        }
        Pet pet = new Pet(new NewPet("fido", Optional.empty()), new PetId(123));
        return new Path_pets_Get_200_Response(Collections.singletonList(pet));
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // override errorResponseBody method to use your own error object from openapi
    // definition
    ////////////////////////////////////////////////////////////////////////////////////

}
