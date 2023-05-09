package pet.store.test;

import java.util.List;
import java.util.Optional;

import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.github.davidmoten.guavamini.Lists;

import pet.store.path.PetsGet200Response;
import pet.store.schema.Error;
import pet.store.schema.NewPet;
import pet.store.schema.Pet;
import pet.store.schema.PetId;
import pet.store.service.Service;

@Component
@Primary
public class PetStoreService implements Service {

    private List<Pet> pets = Lists.newArrayList( //
            new Pet(NewPet.builder().name("fido").tag("A0123").build(), //
                    PetId.id(321L)));

    @Override
    public PetsGet200Response petsGet(Optional<List<String>> tags, int limit) throws ServiceException {
        return PetsGet200Response.petsGet200ResponseItem(pets);
    }

    @Override
    public Pet petsPost(NewPet a) throws ServiceException {
        Pet p = new Pet(a, PetId.id(System.currentTimeMillis()));
        pets.add(p);
        return p;
    }

    @Override
    public Pet petsIdGet(long id) throws ServiceException {
        Optional<Pet> pet = pets.stream().filter(p -> p.petId().id() == id).findAny();
        if (pet.isPresent()) {
            return pet.get();
        } else {
            return response(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Error.message("not found")));
        }
    }

    @Override
    public void petsIdDelete(long id) throws ServiceException {
        Optional<Pet> pet = pets.stream().filter(p -> p.petId().id() == id).findAny();
        if (pet.isPresent()) {
            pets.remove(pet.get());
        } else {
            response(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Error.message("not found")));
        }
    }

    @Override
    public Object errorResponseBody(int statusCode, Throwable e) {
        return Error.message("an error happened");
    }

}
