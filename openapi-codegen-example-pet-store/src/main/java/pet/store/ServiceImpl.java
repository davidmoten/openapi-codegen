package pet.store;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import pet.store.model.NewPet;
import pet.store.model.Path_pets_Get_200_Response;
import pet.store.model.Pet;
import pet.store.model.PetId;

@Component
public class ServiceImpl implements Service {

    @Override
    public Path_pets_Get_200_Response pets(int limit) throws ServiceException {
        if (System.currentTimeMillis() % 2 == 0) {
            throw new ServiceException(405, "huh");
        }
        Pet pet = new Pet(new NewPet("fido", Optional.empty()),new PetId(123));
        return new Path_pets_Get_200_Response(Collections.singletonList(pet));
    }

}
