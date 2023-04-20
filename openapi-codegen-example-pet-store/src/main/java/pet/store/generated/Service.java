package pet.store.generated;

import pet.store.path.PetsGet200Response;
import pet.store.runtime.ErrorHandler;
import pet.store.runtime.ServiceException;

public interface Service extends ErrorHandler {

    default PetsGet200Response pets(int limit) throws ServiceException {
        throw new ServiceException(501, "Not implemented");
    };

}
